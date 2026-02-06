package com.example.curs4.mapper;

import com.example.curs4.entity.Unp;
import com.example.curs4.repository.UnpRepository;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UnpMapperHelper {

    @Autowired
    private UnpRepository unpRepository;

    @Named("mapUnpFromString")
    public Unp mapUnpFromString(String unpStr) {
        // Если УНП пустой или null, возвращаем null
        if (unpStr == null || unpStr.trim().isEmpty()) {
            return null;
        }
        return unpRepository.findByUnp(unpStr)
                .orElseThrow(() -> new IllegalArgumentException("УНП " + unpStr + " не найден в базе данных"));
    }
}