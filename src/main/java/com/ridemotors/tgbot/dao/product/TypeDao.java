package com.ridemotors.tgbot.dao.product;

import com.ridemotors.tgbot.model.product.Type;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TypeDao extends JpaRepository<Type, Long> {

}
