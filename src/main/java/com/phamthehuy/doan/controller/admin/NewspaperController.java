package com.phamthehuy.doan.controller.admin;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.dto.input.NewspaperInsertDTO;
import com.phamthehuy.doan.model.dto.input.NewspaperUpdateDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.model.dto.output.NewspaperOutputDTO;
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
    public List<NewspaperOutputDTO> listNewspaper(@RequestParam(required = false) String sort,
                                                  @RequestParam(required = false) Boolean hidden,
                                                  @RequestParam(required = false) String title,
                                                  @RequestParam Integer page,
                                                  @RequestParam Integer limit) {
        return newspaperService.listNewspaper(sort, hidden, title, page, limit);
    }

    @GetMapping("/new/{id}")
    public NewspaperOutputDTO newspaperDetail(@PathVariable Integer id) throws CustomException {
        return newspaperService.findOneNewspaper(id);
    }

    @PostMapping("/new")
    public NewspaperOutputDTO insertNewspaper(@Valid @RequestBody NewspaperInsertDTO newspaperInsertDTO)
            throws CustomException {
        return newspaperService.insertNewspaper(newspaperInsertDTO);
    }

    @PutMapping("/new/{id}")
    public NewspaperOutputDTO updateNewspaper(@Valid @RequestBody NewspaperUpdateDTO newspaperUpdateDTO,
                                              @PathVariable Integer id)
            throws CustomException {
        return newspaperService.updateNewspaper(newspaperUpdateDTO, id);
    }

    @GetMapping("/new/hidden/{id}")
    public Message hiddenNewspaper(@PathVariable Integer id) throws CustomException {
        return newspaperService.hiddenNewspaper(id);
    }

    @GetMapping("/new/active/{id}")
    public Message activeNewspaper(@PathVariable Integer id) throws CustomException {
        return newspaperService.activeNewspaper(id);
    }

    @DeleteMapping("/new/{id}")
    public Message deleteNewspaper(@PathVariable Integer id) throws CustomException {
        return newspaperService.deleteNewspaper(id);
    }
}
