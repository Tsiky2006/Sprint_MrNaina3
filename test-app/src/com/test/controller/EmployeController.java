package com.test.controller;

import com.framework.annotation.Controller;
import com.framework.annotation.GetMapping;
import com.framework.annotation.Param;
import com.framework.annotation.RequestMapping;

@Controller
@RequestMapping("/employe")
public class EmployeController {

    @GetMapping("/liste")
    public String liste() {
        return "Liste des employés";
    }

    @GetMapping("/detail")
    public String detail(@Param("id") int id) {
        return "Détail employé avec id = " + id;
    }
}