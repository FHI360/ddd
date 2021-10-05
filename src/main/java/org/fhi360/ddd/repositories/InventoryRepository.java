package org.fhi360.ddd.repositories;

import org.fhi360.ddd.domain.Inventory;
import org.fhi360.ddd.domain.Outlet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface  InventoryRepository  extends JpaRepository<Inventory, Long> {

    List<Inventory> findByOutlet(Outlet outlet);
}
