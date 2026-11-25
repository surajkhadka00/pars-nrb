package np.org.nrb.pars.system.repository;

import np.org.nrb.pars.system.model.SysConfig;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysConfigRepository extends JpaRepository<SysConfig, String> {
}
