package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.model.dto.input.ArticleInsertDTO;
import com.phamthehuy.doan.model.dto.input.ArticleUpdateDTO;
import com.phamthehuy.doan.model.dto.output.ArticleOutputDTO;
import com.phamthehuy.doan.service.ArticleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/")
public class ArticleController {
    final
    ArticleService service;

    public ArticleController(ArticleService service) {
        this.service = service;
    }

    @GetMapping("articles")
    public List<ArticleOutputDTO> listArticle(@RequestParam(required = false) Integer page,
                                              @RequestParam(required = false) Integer limit) {
        return service.listArticle(page, limit);
    }

    @DeleteMapping("articles/{id}")
    public ResponseEntity<String> bloclArticle(@PathVariable Integer id) {
        return service.deleteArticle(id);

    }

    @GetMapping("articles/active/{id}")
    public ResponseEntity<String> activeArticle(@PathVariable Integer id) {

        return service.activeArticle(id);
    }

    @PostMapping("articles")
    public ResponseEntity<?> insertArticle(@RequestBody ArticleInsertDTO articleInsertDTO) {
        return service.insertArticle(articleInsertDTO);
    }

    @PutMapping("articles")
    public ResponseEntity<?> updateArticle(@RequestBody ArticleUpdateDTO articleUpdateDTO) {
        return service.updateArticle(articleUpdateDTO);
    }

    @GetMapping("articles/{id}")
    public ResponseEntity<?> findbyID(@PathVariable Integer id) {
        return service.findOneArticle(id);
    }

    @GetMapping(value = "articles", params = "search")
    public List<ArticleOutputDTO> findbyTileAndPhone(@RequestParam String search,
                                                     @RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer limit) {
        return service.findArticleByTitleAndPhone(search, page, limit);
    }

    @GetMapping(value = "articles", params = "post-time=desc")
    public List<ArticleOutputDTO> findbyPostTimeDESC(@RequestParam(required = false) Integer page,
                                                     @RequestParam(required = false) Integer limit) {
        return service.findArticleByPostTimeDESC(page, limit);
    }

    @GetMapping(value = "articles", params = "post-time=asc")
    public List<ArticleOutputDTO> finbyArticleAsc(@RequestParam(required = false) Integer page,
                                                  @RequestParam(required = false) Integer limit){
        return  service.findArticleByPostTimeAsc(page, limit);
    }
    @GetMapping("articles/status-true")
    public List<ArticleOutputDTO> listArticleStatusTrue(@RequestParam(required = false) Integer page,
                                                        @RequestParam(required = false) Integer limit){
        return service.ListAriticleStatusTrue(page, limit);
    }
}
