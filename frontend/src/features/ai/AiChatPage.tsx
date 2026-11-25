import { useEffect, useRef, useState, type FormEvent } from "react";
import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { ArrowRight, Sparkles } from "lucide-react";
import { Alert, Badge, Button, Card, Input } from "../../components/common/ui";
import { AIChatBubble, AIResponseActions, AISourceCard, PageHeader } from "../../components/design/Primitives";
import { aiApi, type AiChatResponse, type AiConversation, type AiMessage } from "../../api/aiApi";
import { useAuth } from "../../auth/AuthContext";
import { useToast } from "../../components/common/Toast";
import { formatDateTime } from "../../lib/format";

const SUGGESTIONS = [
  "What is today's USD exchange rate?",
  "Show the last 3 months of NRB forex rates",
  "Find the latest foreign exchange circular",
  "What is my username?",
];

export default function AiChatPage() {
  const { hasPermission } = useAuth();
  const { push } = useToast();
  const queryClient = useQueryClient();
  const [conversationId, setConversationId] = useState<string | null>(null);
  const [question, setQuestion] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [copied, setCopied] = useState(false);
  const scrollRef = useRef<HTMLDivElement>(null);

  const conversationsQuery = useQuery({
    queryKey: ["ai", "conversations"],
    queryFn: () => aiApi.conversations(),
    enabled: hasPermission("AI_CONVERSATION_READ"),
  });

  const messagesQuery = useQuery({
    queryKey: ["ai", "messages", conversationId],
    queryFn: () => aiApi.messages(conversationId as string),
    enabled: !!conversationId && hasPermission("AI_CONVERSATION_READ"),
  });

  const chatMutation = useMutation<AiChatResponse, Error, string>({
    mutationFn: (text: string) => aiApi.chat(text, conversationId ?? undefined),
    onSuccess: (result) => {
      setConversationId(result.conversationId);
      setQuestion("");
      setError(null);
      void queryClient.invalidateQueries({ queryKey: ["ai", "conversations"] });
      void queryClient.invalidateQueries({ queryKey: ["ai", "messages", result.conversationId] });
    },
    onError: (cause) => setError(cause.message || "Unable to get an answer"),
  });

  const feedbackMutation = useMutation({
    mutationFn: (vars: { messageId: string; rating: number }) => aiApi.feedback(vars.messageId, vars.rating, ""),
    onSuccess: () => push("success", "Thank you for your feedback"),
  });

  const messages: AiMessage[] = messagesQuery.data ?? [];
  const conversations: AiConversation[] = conversationsQuery.data ?? [];

  useEffect(() => {
    const element = scrollRef.current;
    if (element) {
      element.scrollTop = element.scrollHeight;
    }
  }, [messages.length, chatMutation.isPending]);

  if (!hasPermission("AI_CHAT")) {
    return <Alert variant="destructive">You do not have permission to use the PARS AI assistant.</Alert>;
  }

  function submit(event?: FormEvent) {
    event?.preventDefault();
    const text = question.trim();
    if (!text || chatMutation.isPending) {
      return;
    }
    chatMutation.mutate(text);
  }

  function askSuggestion(text: string) {
    setQuestion(text);
    chatMutation.mutate(text);
  }

  return (
    <div className="space-y-4">
      <PageHeader
        eyebrow="Intelligence"
        title="PARS AI Assistant"
        description="Evidence-grounded answers from approved PARS NRB knowledge and live official data"
        actions={<Badge variant="outline" className="gap-1"><Sparkles className="h-3 w-3" /> Grounded</Badge>}
      />

      <div className="grid gap-4 lg:grid-cols-[260px_1fr]">
        <Card className="pars-card h-fit">
          <div className="p-3">
            <Button className="w-full" onClick={() => { setConversationId(null); setQuestion(""); setError(null); }}>
              New conversation
            </Button>
          </div>
          <div className="max-h-[60vh] overflow-y-auto border-t border-slate-100 px-2 py-2 dark:border-slate-800">
            {conversations.length === 0 ? <p className="px-2 py-3 text-xs text-slate-500">No conversations yet.</p> : null}
            {conversations.map((conversation) => (
              <button
                key={conversation.id}
                type="button"
                onClick={() => setConversationId(conversation.id)}
                className={`mb-1 block w-full rounded-lg px-3 py-2 text-left text-xs transition ${conversation.id === conversationId ? "bg-navy-100 text-navy-900 dark:bg-navy-800 dark:text-white" : "text-slate-600 hover:bg-slate-50 dark:text-slate-300 dark:hover:bg-slate-800"}`}
              >
                <span className="block truncate font-medium">{conversation.title}</span>
                <span className="text-slate-400">{formatDateTime(conversation.updatedAt)}</span>
              </button>
            ))}
          </div>
        </Card>

        {/* Chat card: fixed viewport height; answer always appears just above the composer */}
        <Card className="pars-card flex h-[calc(100vh-230px)] min-h-[420px] flex-col overflow-hidden">
          <div className="flex items-center justify-between border-b border-slate-100 px-4 py-3 dark:border-slate-800">
            <div>
              <p className="text-sm font-semibold">Conversation</p>
              <p className="text-[11px] text-slate-500">{messages.length} message(s) · Nepal Standard Time</p>
            </div>
            {chatMutation.data?.model ? <Badge variant="secondary" className="text-[10px]">{chatMutation.data.model}</Badge> : null}
          </div>

          <div ref={scrollRef} className="min-h-0 flex-1 space-y-4 overflow-y-auto bg-slate-50/60 px-4 py-4 dark:bg-slate-950/30">
            {messages.length === 0 && !chatMutation.isPending ? (
              <div className="flex h-full flex-col items-center justify-center text-center">
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-navy-800 text-green-accent-300"><Sparkles className="h-6 w-6" /></div>
                <p className="mt-4 text-base font-semibold">How can I help you today?</p>
                <p className="mt-1 max-w-md text-sm text-slate-500">Ask about PARS procedures, official NRB forex rates, circulars or your own account details.</p>
                <div className="mt-5 grid w-full max-w-xl gap-2 sm:grid-cols-2">
                  {SUGGESTIONS.map((suggestion) => (
                    <button key={suggestion} type="button" onClick={() => askSuggestion(suggestion)} className="flex items-center justify-between rounded-lg border border-slate-200 bg-white px-3 py-2.5 text-left text-xs hover:border-navy-300 dark:border-slate-700 dark:bg-slate-900">
                      <span>{suggestion}</span><ArrowRight className="h-3 w-3 text-slate-400" />
                    </button>
                  ))}
                </div>
              </div>
            ) : null}

            {messages.map((message) => (
              <AIChatBubble key={message.id} role={message.role === "USER" ? "user" : "assistant"} timestamp={message.createdAt ? formatDateTime(message.createdAt) : undefined}>
                <p className="whitespace-pre-wrap">{message.content}</p>
                {message.insufficientEvidence ? (
                  <div className="mt-3 rounded-md border border-amber-200 bg-amber-50 p-2 text-xs dark:border-amber-900/50 dark:bg-amber-950/30" role="status">
                    <p className="font-medium text-amber-800 dark:text-amber-300">Insufficient evidence</p>
                    <p className="mt-1 text-amber-700 dark:text-amber-400">
                      Verify on the official NRB site:{" "}
                      <a href="https://www.nrb.org.np/" target="_blank" rel="noopener noreferrer" className="underline">nrb.org.np</a>
                      {" · "}
                      <a href="https://www.nrb.org.np/api-docs-v1/" target="_blank" rel="noopener noreferrer" className="underline">Forex API docs</a>
                    </p>
                  </div>
                ) : null}
                {message.sources?.length > 0 ? (
                  <div className="mt-3 space-y-2 border-t border-slate-200 pt-3 dark:border-slate-700">
                    <p className="text-[11px] font-semibold uppercase tracking-wider text-slate-500">Sources</p>
                    {message.sources.map((source) => <AISourceCard key={source.chunkId + source.page} title={source.documentTitle} url={source.sourceUrl} page={source.page} section={source.section} />)}
                  </div>
                ) : null}
                {message.role === "ASSISTANT" ? (
                  <div className="mt-3">
                    <AIResponseActions
                      copied={copied}
                      onCopy={() => {
                        void navigator.clipboard.writeText(message.content);
                        setCopied(true);
                        push("success", "Answer copied");
                      }}
                      onRetry={() => question && chatMutation.mutate(question)}
                      onFeedback={hasPermission("AI_FEEDBACK") ? (rating) => feedbackMutation.mutate({ messageId: message.id, rating: rating === "up" ? 5 : 1 }) : undefined}
                    />
                  </div>
                ) : null}
              </AIChatBubble>
            ))}

            {chatMutation.isPending ? (
              <div className="flex items-center gap-2 text-sm text-slate-500" role="status">
                <span className="h-2 w-2 animate-pulse rounded-full bg-green-accent-500" /> PARS AI is thinking…
              </div>
            ) : null}
            {error ? <Alert variant="destructive">{error}<Button size="sm" className="ml-2" onClick={() => question && chatMutation.mutate(question)}>Retry</Button></Alert> : null}
          </div>

          {/* Composer pinned directly under the latest answer */}
          <form onSubmit={submit} className="border-t border-slate-200 bg-white p-3 dark:border-slate-700 dark:bg-slate-900">
            <div className="flex items-end gap-2">
              <Input
                value={question}
                onChange={(event) => setQuestion(event.target.value)}
                placeholder="Ask about policies, forex rates, circulars or your account…"
                maxLength={8000}
                disabled={chatMutation.isPending}
                aria-label="Ask PARS AI"
                onKeyDown={(event) => {
                  if (event.key === "Enter" && !event.shiftKey) {
                    event.preventDefault();
                    submit();
                  }
                }}
              />
              <Button type="submit" disabled={!question.trim() || chatMutation.isPending} aria-label="Send question">
                Ask <ArrowRight className="h-4 w-4" />
              </Button>
            </div>
            <p className="mt-2 text-[10px] text-slate-400">Answers are grounded in approved sources. Verify critical figures against the linked official NRB document.</p>
          </form>
        </Card>
      </div>
    </div>
  );
}
