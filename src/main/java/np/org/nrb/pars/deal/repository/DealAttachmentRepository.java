package np.org.nrb.pars.deal.repository;

import np.org.nrb.pars.deal.model.DealAttachment;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DealAttachmentRepository extends JpaRepository<DealAttachment, UUID> {

    @EntityGraph(attributePaths = "uploadedBy")
    List<DealAttachment> findByDeal_IdOrderByCreatedAtDesc(UUID dealId);

    @EntityGraph(attributePaths = {"deal", "uploadedBy"})
    Optional<DealAttachment> findDetailedById(UUID id);
}
