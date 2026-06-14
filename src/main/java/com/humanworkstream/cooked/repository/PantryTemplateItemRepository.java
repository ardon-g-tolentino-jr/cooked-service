package com.humanworkstream.cooked.repository;

import com.humanworkstream.cooked.entity.PantryTemplateItem;
import com.humanworkstream.cooked.entity.id.PantryTemplateItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PantryTemplateItemRepository extends JpaRepository<PantryTemplateItem, PantryTemplateItemId> {

    List<PantryTemplateItem> findByIdTemplateId(Long templateId);

    List<PantryTemplateItem> findByIdTemplateIdIn(List<Long> templateIds);
}
