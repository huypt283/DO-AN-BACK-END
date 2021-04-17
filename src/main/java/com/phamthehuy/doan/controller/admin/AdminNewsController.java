package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.model.request.NewsInsertRequest;
import com.phamthehuy.doan.model.request.NewsUpdateRequest;
import com.phamthehuy.doan.service.NewsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/admin/news")
public class AdminNewsController {
    @Autowired
    private NewsService newsService;

    @GetMapping
    public ResponseEntity<?> listAllNews() {
        return new ResponseEntity<>(newsService.listAllNews(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getNewsById(@PathVariable Integer id) throws Exception {
        return new ResponseEntity<>(newsService.findNewsById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> insertNews(@Valid @RequestBody NewsInsertRequest newsInsertRequest,
                                        @AuthenticationPrincipal UserDetails currentUser)
            throws Exception {
        return new ResponseEntity<>(newsService.insertNews(newsInsertRequest, currentUser), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateNewsById(@PathVariable Integer id,
                                            @Valid @RequestBody NewsUpdateRequest newsUpdateRequest) throws Exception {
        return new ResponseEntity<>(newsService.updateNewsById(id, newsUpdateRequest), HttpStatus.OK);
    }

    @PutMapping("/hide/{id}")
    public ResponseEntity<?> hideNewsById(@PathVariable Integer id) throws Exception {
        return new ResponseEntity<>(newsService.hideNewsById(id), HttpStatus.OK);
    }

    @PutMapping("/active/{id}")
    public ResponseEntity<?> activeNewsById(@PathVariable Integer id) throws Exception {
        return new ResponseEntity<>(newsService.activeNewsById(id), HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteNewsById(@PathVariable Integer id) throws Exception {
        return new ResponseEntity<>(newsService.deleteNewsById(id), HttpStatus.OK);
    }
}
