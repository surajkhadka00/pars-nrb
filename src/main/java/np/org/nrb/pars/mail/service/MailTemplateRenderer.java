package np.org.nrb.pars.mail.service;

import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.mail.model.MailTemplate;
import np.org.nrb.pars.mail.repository.MailTemplateRepository;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class MailTemplateRenderer {

    private final MailTemplateRepository mailTemplateRepository;

    public MailTemplateRenderer(MailTemplateRepository mailTemplateRepository) {
        this.mailTemplateRepository = mailTemplateRepository;
    }

    public RenderedMail render(String templateCode, Map<String, String> variables) {
        MailTemplate template = mailTemplateRepository.findById(templateCode)
                .orElseThrow(() -> Exceptions.badRequest("Mail template " + templateCode + " is not configured"));
        return new RenderedMail(apply(template.getSubject(), variables), apply(template.getBody(), variables));
    }

    private static String apply(String template, Map<String, String> variables) {
        String result = template;
        for (Map.Entry<String, String> entry : variables.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", escape(entry.getValue()));
        }
        return result;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
