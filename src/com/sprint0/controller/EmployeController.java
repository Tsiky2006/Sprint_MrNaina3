package com.sprint0.controller;

import com.sprint0.annotation.Controller;
import com.sprint0.annotation.RequestMapping;
import com.sprint0.annotation.GetMapping;
import com.sprint0.annotation.Param;

@Controller
@RequestMapping("/employe")
public class EmployeController {

    @GetMapping("/liste")
    public String liste() {
        return "Liste des employés";
    }

    @GetMapping("/detail")
    public String detail() {
        return "Détail employé";
    }

    @GetMapping("/detail")
    public String detail(@Param("id") int id) {
    return "Détail employé avec id = " + id;
    }
}