package com.ridemotors.tgbot.dao;

import com.ridemotors.tgbot.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceDao extends JpaRepository<Resource, String> {

    @Query("SELECT r FROM resource r where r.productId =:productId")
    public List<Resource> getResourceByProduct(@Param("productId") Long productId);

    @Query("SELECT r FROM resource r where r.productId =:productId and r.type=:type")
    public List<Resource> getResourceByType(@Param("productId") Long productId, @Param("type") String type);
}
