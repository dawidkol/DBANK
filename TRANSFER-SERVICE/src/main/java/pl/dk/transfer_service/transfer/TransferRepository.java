package pl.dk.transfer_service.transfer;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
interface TransferRepository extends JpaRepository<Transfer, String> {

}
