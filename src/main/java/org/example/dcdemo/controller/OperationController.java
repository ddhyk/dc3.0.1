package org.example.dcdemo.controller;

import org.example.dcdemo.model.Operation;
import org.example.dcdemo.service.OperationStaffService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/operation-staff")
public class OperationController {
    @Autowired
    private OperationStaffService operationStaffService;
    
    @PostMapping("/add")
    @ResponseBody
    public String addStaff(@RequestBody Operation staff) {
        try {
            operationStaffService.createStaff(staff);
            return "success";
        } catch (Exception e) {
            return "error: " + e.getMessage();
        }
    }
} 