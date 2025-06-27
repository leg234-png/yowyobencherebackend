package com.yowyob.dev.repositories;

import com.yowyob.dev.models.Category;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CategoryRepository extends R2dbcRepository<Category, UUID> {
}