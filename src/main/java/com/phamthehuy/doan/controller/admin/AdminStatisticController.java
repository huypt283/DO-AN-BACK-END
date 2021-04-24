package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.entity.ArticleStatistic;
import com.phamthehuy.doan.model.response.ArticleStatisticResponse;
import com.phamthehuy.doan.repository.ArticleStatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/statistics")
public class AdminStatisticController {
    @Autowired
    private ArticleStatisticRepository articleStatisticRepository;

    @GetMapping("/article")
    public ResponseEntity<?> articleStatistic() {
        List<ArticleStatistic> articleStatistics = articleStatisticRepository.findAll();
        articleStatistics.add(0, new ArticleStatistic(0, "3/2021", 20));
        articleStatistics.add(0, new ArticleStatistic(0, "2/2021", 15));
        articleStatistics.add(0, new ArticleStatistic(0, "1/2021", 18));
        articleStatistics.add(0, new ArticleStatistic(0, "12/2010", 20));
        articleStatistics.add(0, new ArticleStatistic(0, "11/2020", 16));
        return new ResponseEntity<>(articleStatistics.stream()
                .map(this::convertToArticleStatisticResponse).collect(Collectors.toList())
                , HttpStatus.OK);
    }
    
    private ArticleStatisticResponse convertToArticleStatisticResponse(ArticleStatistic articleStatistic) {
        return new ArticleStatisticResponse(articleStatistic.getTime(), articleStatistic.getCount());
    }
}
