package np.org.nrb.pars.mail.repository;

import np.org.nrb.pars.mail.model.MailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailTemplateRepository extends JpaRepository<MailTemplate, String> {
}
