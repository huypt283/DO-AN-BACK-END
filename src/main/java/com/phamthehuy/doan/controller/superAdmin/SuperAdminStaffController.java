package com.phamthehuy.doan.controller.superAdmin;

import com.phamthehuy.doan.model.request.StaffInsertRequest;
import com.phamthehuy.doan.model.request.StaffUpdateRequest;
import com.phamthehuy.doan.service.StaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/super-admin/staffs")
public class SuperAdminStaffController {
    @Autowired
    private StaffService staffService;

    @GetMapping
    public ResponseEntity<?> listAllStaff() {
        return new ResponseEntity<>(staffService.listAllStaff(), HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> findStaffById(@PathVariable Integer id) throws Exception {
        return new ResponseEntity<>(staffService.findStaffById(id), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> insertStaff(@Valid @RequestBody StaffInsertRequest staffInsertRequest) throws Exception {
        return new ResponseEntity<>(staffService.insertStaff(staffInsertRequest), HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaffById(@PathVariable Integer id,
                                             @Valid @RequestBody StaffUpdateRequest staffUpdateRequest) throws Exception {
        return new ResponseEntity<>(staffService.updateStaffById(id, staffUpdateRequest), HttpStatus.OK);
    }

    @PostMapping("/active/{id}")
    public ResponseEntity<?> activeStaffById(@PathVariable Integer id) throws Exception {
        return new ResponseEntity<>(staffService.activeStaffById(id), HttpStatus.OK);
    }

    @PostMapping("/block/{id}")
    public ResponseEntity<?> blockStaffById(@PathVariable Integer id) throws Exception {
        return new ResponseEntity<>(staffService.blockStaffById(id), HttpStatus.OK);
    }

//    @DeleteMapping("{id}")
//    public ResponseEntity<?> deleteStaffById(@PathVariable Integer id) throws Exception {
//        return new ResponseEntity<>(staffService.deleteStaffById(id), HttpStatus.OK);
//    }

//    @GetMapping("/staffs")
//    public List<StaffResponse> listStaffs
//            (@RequestParam(required = false) String search,
//             @RequestParam(required = false) Boolean block,
//             @RequestParam(required = false) String sort,
//             @RequestParam Integer page,
//             @RequestParam Integer limit) {
//        return staffService.listStaff(search, block, sort, page, limit);
//    }
}
