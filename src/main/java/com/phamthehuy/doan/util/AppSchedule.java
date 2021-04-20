package com.phamthehuy.doan.util;

import com.phamthehuy.doan.entity.Article;
import com.phamthehuy.doan.entity.ArticleStatistic;
import com.phamthehuy.doan.repository.ArticleRepository;
import com.phamthehuy.doan.repository.ArticleStatisticRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class AppSchedule {
    @Autowired
    private ArticleRepository articleRepository;
    @Autowired
    private ArticleStatisticRepository articleStatisticRepository;
    @Autowired
    private MailSender mailSender;

    @Scheduled(cron = "0 0 0 * * *")
    public void AutoArticleHandle() {
        List<Article> articles = articleRepository.findByDeletedFalse();

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        if (articles != null && articles.size() > 0) {
            for (Article article : articles) {
                //ẩn bài đăng hết hạn
                if (new Date().after(article.getExpTime())) {
                    article.setDeleted(true);
                    articleRepository.save(article);

                    mailSender.send(
                            article.getCustomer().getEmail(),
                            "Bài đăng số: " + article.getArticleId() + " của đã hết hạn",
                            "<p><strong>Chúng tôi xin trân trọng thông báo:</strong></p>\n" +
                                    "<p>Bài đăng số: " + article.getArticleId() + "</p>\n" +
                                    "<p>Tiêu đề: " + article.getTitle() + "</p>\n" +
                                    "<p>Đã hết hạn vào ngày: <span style=\"color: #0000ff;\">" + sdf.format(article.getExpTime()) + "</span></p>\n" +
                                    "<p>Nếu bạn muốn bài đăng tiếp tục được hiển thị vui lòng gia hạn thêm thời gian.</p>",
                            "Xin cảm ơn quý khách đã sử dụng dịch vụ của chúng tôi."
                    );
                } else {
                    //gửi mail bài đăng sắp hết hạn
                    if (new Date().getTime() - article.getExpTime().getTime() < 50 * 3600 * 1000) {
                        mailSender.send(
                                article.getCustomer().getEmail(),
                                "Bài đăng số: " + article.getArticleId() + " của bạn sắp hết hạn",
                                "<p><strong>Chúng tôi xin trân trọng thông báo:</strong></p>\n" +
                                        "<p>Bài đăng số: " + article.getArticleId() + "</p>\n" +
                                        "<p>Tiêu đề: " + article.getTitle() + "</p>\n" +
                                        "<p>Sẽ hết hạn vào ngày: <span style=\"color: #0000ff;\">" + sdf.format(article.getExpTime()) + "</span></p>\n" +
                                        "<p>Nếu bạn muốn bài đăng tiếp tục được hiển thị vui lòng gia hạn bài đăng trước thời hạn trên.</p>",
                                "Xin cảm ơn quý khách đã sử dụng dịch vụ của chúng tôi."
                        );
                    }
                }
            }
        }
    }

    @Scheduled(cron = "0 0 20 * * *")
//    @Scheduled(cron = "0 */5 * * * *")
    public void AutoArticleStatistic() {
        System.out.println("Cron go here OK");
        List<Article> articles = articleRepository.findAll();
        Map<String, Integer> statistic = new HashMap<>();
        if (articles.size() > 0) {
            articles.forEach(article -> {
                Calendar cal = Calendar.getInstance();
                cal.setTime(article.getTimeCreated());
                String key = cal.get(Calendar.MONTH) + 1 + "/" + cal.get(Calendar.YEAR);

                statistic.computeIfAbsent(key, total -> 0);
                statistic.put(key, statistic.get(key) + 1);
            });
        }

        statistic.forEach((k, v) -> {
            ArticleStatistic articleStatistic = articleStatisticRepository.findByTime(k);
            if (articleStatistic == null) {
                articleStatistic = new ArticleStatistic();
                articleStatistic.setTime(k);
            }
            articleStatistic.setCount(v);
            articleStatisticRepository.save(articleStatistic);
        });
    }
}
