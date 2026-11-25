package np.org.nrb.pars.common.query;

import np.org.nrb.pars.common.exception.Exceptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

final class ODataFilterParser {

    private ODataFilterParser() {
    }

    static Node parse(String filter) {
        if (filter == null || filter.isBlank()) {
            return null;
        }
        if (filter.length() > 2000) {
            throw Exceptions.badRequest("Filter is too long");
        }
        Parser parser = new Parser(filter);
        Node node = parser.parseOr();
        parser.expectEnd();
        return node;
    }

    sealed interface Node permits Comparison, And, Or {
    }

    record Comparison(String path, String operator, String rawValue, ValueKind valueKind) implements Node {
    }

    record And(Node left, Node right) implements Node {
    }

    record Or(Node left, Node right) implements Node {
    }

    enum ValueKind {
        STRING, NUMBER, BOOLEAN, NULL
    }

    private static final class Parser {
        private final List<Token> tokens;
        private int index;

        private Parser(String input) {
            this.tokens = tokenize(input);
        }

        private Node parseOr() {
            Node left = parseAnd();
            while (match(TokenType.OR)) {
                left = new Or(left, parseAnd());
            }
            return left;
        }

        private Node parseAnd() {
            Node left = parsePrimary();
            while (match(TokenType.AND)) {
                left = new And(left, parsePrimary());
            }
            return left;
        }

        private Node parsePrimary() {
            if (match(TokenType.LPAREN)) {
                Node inner = parseOr();
                expect(TokenType.RPAREN, "Expected ')'");
                return inner;
            }
            Token path = expect(TokenType.IDENT, "Expected field name");
            Token op = current();
            if (op.type() != TokenType.EQ && op.type() != TokenType.NE && op.type() != TokenType.GT
                    && op.type() != TokenType.GE && op.type() != TokenType.LT && op.type() != TokenType.LE) {
                throw Exceptions.badRequest("Expected comparison operator after '" + path.value() + "'");
            }
            index++;
            Token value = current();
            ValueKind kind;
            String raw;
            switch (value.type()) {
                case STRING -> {
                    kind = ValueKind.STRING;
                    raw = value.value();
                    index++;
                }
                case NUMBER -> {
                    kind = ValueKind.NUMBER;
                    raw = value.value();
                    index++;
                }
                case TRUE, FALSE -> {
                    kind = ValueKind.BOOLEAN;
                    raw = value.value();
                    index++;
                }
                case NULL -> {
                    kind = ValueKind.NULL;
                    raw = null;
                    index++;
                }
                case IDENT -> {
                    kind = ValueKind.STRING;
                    raw = value.value();
                    index++;
                }
                default -> throw Exceptions.badRequest("Expected a value after operator");
            }
            return new Comparison(path.value(), op.value(), raw, kind);
        }

        private boolean match(TokenType type) {
            if (current().type() == type) {
                index++;
                return true;
            }
            return false;
        }

        private Token expect(TokenType type, String message) {
            Token token = current();
            if (token.type() != type) {
                throw Exceptions.badRequest(message);
            }
            index++;
            return token;
        }

        private void expectEnd() {
            if (current().type() != TokenType.EOF) {
                throw Exceptions.badRequest("Unexpected token in filter: " + current().value());
            }
        }

        private Token current() {
            return tokens.get(index);
        }
    }

    private enum TokenType {
        IDENT, STRING, NUMBER, TRUE, FALSE, NULL, EQ, NE, GT, GE, LT, LE, AND, OR, LPAREN, RPAREN, EOF
    }

    private record Token(TokenType type, String value) {
    }

    private static List<Token> tokenize(String input) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        while (i < input.length()) {
            char c = input.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '(') {
                tokens.add(new Token(TokenType.LPAREN, "("));
                i++;
                continue;
            }
            if (c == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")"));
                i++;
                continue;
            }
            if (c == '\'') {
                StringBuilder text = new StringBuilder();
                i++;
                while (i < input.length()) {
                    char ch = input.charAt(i);
                    if (ch == '\'') {
                        if (i + 1 < input.length() && input.charAt(i + 1) == '\'') {
                            text.append('\'');
                            i += 2;
                            continue;
                        }
                        i++;
                        break;
                    }
                    text.append(ch);
                    i++;
                }
                tokens.add(new Token(TokenType.STRING, text.toString()));
                continue;
            }
            if (c == '-' || Character.isDigit(c)) {
                int start = i;
                if (c == '-') {
                    i++;
                }
                while (i < input.length() && (Character.isDigit(input.charAt(i)) || input.charAt(i) == '.')) {
                    i++;
                }
                tokens.add(new Token(TokenType.NUMBER, input.substring(start, i)));
                continue;
            }
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < input.length()) {
                    char ch = input.charAt(i);
                    if (Character.isLetterOrDigit(ch) || ch == '_' || ch == '.' || ch == '/') {
                        i++;
                    } else {
                        break;
                    }
                }
                String word = input.substring(start, i);
                String lower = word.toLowerCase(Locale.ROOT);
                tokens.add(switch (lower) {
                    case "eq" -> new Token(TokenType.EQ, "eq");
                    case "ne" -> new Token(TokenType.NE, "ne");
                    case "gt" -> new Token(TokenType.GT, "gt");
                    case "ge" -> new Token(TokenType.GE, "ge");
                    case "lt" -> new Token(TokenType.LT, "lt");
                    case "le" -> new Token(TokenType.LE, "le");
                    case "and" -> new Token(TokenType.AND, "and");
                    case "or" -> new Token(TokenType.OR, "or");
                    case "true" -> new Token(TokenType.TRUE, "true");
                    case "false" -> new Token(TokenType.FALSE, "false");
                    case "null" -> new Token(TokenType.NULL, "null");
                    default -> new Token(TokenType.IDENT, word.replace('/', '.'));
                });
                continue;
            }
            throw Exceptions.badRequest("Invalid character in filter: " + c);
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}
