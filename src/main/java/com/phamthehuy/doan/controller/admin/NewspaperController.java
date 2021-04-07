package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.request.NewsInsertRequest;
import com.phamthehuy.doan.model.request.NewsUpdateRequest;
import com.phamthehuy.doan.model.response.MessageResponse;
import com.phamthehuy.doan.model.response.NewsResponse;
import com.phamthehuy.doan.service.NewspaperService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class NewspaperController {
    final
    NewspaperService newspaperService;

    public NewspaperController(NewspaperService newspaperService) {
        this.newspaperService = newspaperService;
    }

    @GetMapping("/new")
    public List<NewsResponse> listNewspaper(@RequestParam(required = false) String sort,
                                            @RequestParam(required = false) Boolean hidden,
                                            @RequestParam(required = false) String title,
                                            @RequestParam Integer page,
                                            @RequestParam Integer limit) {
        return newspaperService.listNewspaper(sort, hidden, title, page, limit);
    }

    @GetMapping("/new/{id}")
    public NewsResponse newspaperDetail(@PathVariable Integer id) throws CustomException {
        return newspaperService.findOneNewspaper(id);
    }

    @PostMapping("/new")
    public NewsResponse insertNewspaper(@Valid @RequestBody NewsInsertRequest newsInsertRequest)
            throws CustomException {
        return newspaperService.insertNewspaper(newsInsertRequest);
    }

    @PutMapping("/new/{id}")
    public NewsResponse updateNewspaper(@Valid @RequestBody NewsUpdateRequest newsUpdateRequest,
                                        @PathVariable Integer id)
            throws CustomException {
        return newspaperService.updateNewspaper(newsUpdateRequest, id);
    }

    @GetMapping("/new/hidden/{id}")
    public MessageResponse hiddenNewspaper(@PathVariable Integer id) throws CustomException {
        return newspaperService.hiddenNewspaper(id);
    }

    @GetMapping("/new/active/{id}")
    public MessageResponse activeNewspaper(@PathVariable Integer id) throws CustomException {
        return newspaperService.activeNewspaper(id);
    }

    @DeleteMapping("/new/{id}")
    public MessageResponse deleteNewspaper(@PathVariable Integer id) throws CustomException {
        return newspaperService.deleteNewspaper(id);
    }
}
