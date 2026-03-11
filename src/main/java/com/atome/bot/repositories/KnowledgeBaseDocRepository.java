package com.atome.bot.repositories;

import com.atome.bot.model.KnowledgeBaseDoc;
import org.springframework.data.jpa.repository.JpaRepository;

public interface KnowledgeBaseDocRepository extends JpaRepository<KnowledgeBaseDoc,String> {
}
