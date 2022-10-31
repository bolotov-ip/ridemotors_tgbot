package com.ridemotors.tgbot.dao.product;

import com.ridemotors.tgbot.model.product.Character;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CharacterDao extends JpaRepository<Character, Long> {

    @Query("SELECT c FROM character c where c.productTypeId =:productTypeId")
    public List<Character> getCharacterByProductTypeId(@Param("productTypeId") Long productTypeId);

    default public void removeCharacterByProductId(Long productTypeId){
        List<Character> characters = getCharacterByProductTypeId(productTypeId);
        this.deleteAll(characters);
    }
}
