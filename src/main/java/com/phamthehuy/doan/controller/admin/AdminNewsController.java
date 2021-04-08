package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.model.request.NewsInsertRequest;
import com.phamthehuy.doan.model.request.NewsUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.NewsResponse;
import com.phamthehuy.doan.service.NewsService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin/news")
public class AdminNewsController {
    final
    NewsService newsService;

    public AdminNewsController(NewsService newsService) {
        this.newsService = newsService;
    }

    @GetMapping
    public List<NewsResponse> listNewspaper(@RequestParam(required = false) String sort,
                                            @RequestParam(required = false) Boolean hidden,
                                            @RequestParam(required = false) String title,
                                            @RequestParam Integer page,
                                            @RequestParam Integer limit) {
        return newsService.listNews(sort, hidden, title, page, limit);
    }

    @GetMapping("/{id}")
    public NewsResponse newspaperDetail(@PathVariable Integer id) throws BadRequestException {
        return newsService.findNewsById(id);
    }

    @PostMapping
    public NewsResponse insertNewspaper(@Valid @RequestBody NewsInsertRequest newsInsertRequest)
            throws BadRequestException {
        return newsService.insertNews(newsInsertRequest);
    }

    @PutMapping("/{id}")
    public NewsResponse updateNewspaper(@Valid @RequestBody NewsUpdateRequest newsUpdateRequest,
                                        @PathVariable Integer id)
            throws BadRequestException {
        return newsService.updateNews(newsUpdateRequest, id);
    }

    @PutMapping("/hide/{id}")
    public MessageResponse hiddenNewspaper(@PathVariable Integer id) throws BadRequestException {
        return newsService.hideNews(id);
    }

    @PutMapping("/show/{id}")
    public MessageResponse activeNewspaper(@PathVariable Integer id) throws BadRequestException {
        return newsService.activeNews(id);
    }

    @DeleteMapping("/{id}")
    public MessageResponse deleteNewspaper(@PathVariable Integer id) throws BadRequestException {
        return newsService.delteNews(id);
    }
}
