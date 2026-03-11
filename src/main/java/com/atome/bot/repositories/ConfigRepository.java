package com.atome.bot.repositories;

import com.atome.bot.model.Configuration;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigRepository extends JpaRepository<Configuration,String> {
}
