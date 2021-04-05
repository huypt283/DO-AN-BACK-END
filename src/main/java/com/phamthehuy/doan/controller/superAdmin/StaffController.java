package com.phamthehuy.doan.controller.superAdmin;

import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.model.dto.input.StaffInsertDTO;
import com.phamthehuy.doan.model.dto.input.StaffUpdateDTO;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.model.dto.output.StaffOutputDTO;
import com.phamthehuy.doan.service.StaffService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/super-admin")
public class StaffController {
    final
    StaffService staffService;

    public StaffController(StaffService staffService) {
        this.staffService = staffService;
    }

    //page là trang mấy
    //limit là số bản ghi trong 1 trang
    //nếu ko nhập 2 tham số này thì ko phân trang
    //search theo name or mail or phone ko nhập thì trả về all
    //sort=asc or desc không nhập thì ko xếp
    @GetMapping("/staffs")
    public List<StaffOutputDTO> listStaffs
            (@RequestParam(required = false) String search,
             @RequestParam(required = false) Boolean block,
             @RequestParam(required = false) String sort,
             @RequestParam Integer page,
             @RequestParam Integer limit) {
        return staffService.listStaff(search, block, sort, page, limit);
    }

    //    thêm nhân viên	Post/super-admin/staffs
    @PostMapping("/staffs")
    public Message insertStaff(@Valid @RequestBody StaffInsertDTO staffInsertDTO,
                               HttpServletRequest request)
            throws Exception {
        return staffService.insertStaff(staffInsertDTO, request);
    }

    //    cập nhật thông tin nhân viên	Put/super-admin/staffs
    @PutMapping("/staffs/{id}")
    public ResponseEntity<?> updateStaff(@Valid @RequestBody StaffUpdateDTO staffUpdateDTO,
                                         @PathVariable Integer id)
    throws CustomException {
        return staffService.updateStaff(staffUpdateDTO, id);
    }


    @GetMapping("/staffs/block/{id}")
    public Message blockStaff(@PathVariable Integer id) throws CustomException{
        return staffService.blockStaff(id);
    }


    @GetMapping("/staffs/active/{id}")
    public Message activeStaff(@PathVariable Integer id) throws CustomException{
        return staffService.activeStaff(id);
    }

    //    xem thông tin nhân viên	GET/super-admin/staffs/{id}
    @GetMapping("/staffs/{id}")
    public ResponseEntity<?> findOneStaff(@PathVariable Integer id) {
        return staffService.findOneStaff(id);
    }

    // xóa toàn bộ những nhân viên đã bị xóa mềm
    @DeleteMapping("/staffs")
    public Message deleteAllStaffs(){
        return staffService.deleteAllStaffs();
    }

    @DeleteMapping("/staffs/{id}")
    public Message deleteStaffs(@PathVariable Integer id) throws CustomException{
        return staffService.deleteStaffs(id);
    }
}
