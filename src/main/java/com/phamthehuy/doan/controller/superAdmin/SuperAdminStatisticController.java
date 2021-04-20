package com.phamthehuy.doan.controller.superAdmin;

import com.phamthehuy.doan.entity.ArticleStatistic;
import com.phamthehuy.doan.repository.ArticleStatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/super-admin/statistics")
public class SuperAdminStatisticController {
    @Autowired
    private ArticleStatisticRepository articleStatisticRepository;

    @GetMapping("/article")
    public ResponseEntity<?> articleStatistic() {
        List<ArticleStatistic> articleStatistics = articleStatisticRepository.findAll();
        articleStatistics.add(0, new ArticleStatistic(-1, "3/2021", 20));
        articleStatistics.add(0, new ArticleStatistic(-2, "2/2021", 15));
        articleStatistics.add(0, new ArticleStatistic(-3, "1/2021", 10));
        return new ResponseEntity<>(articleStatistics, HttpStatus.OK);
    }
}
