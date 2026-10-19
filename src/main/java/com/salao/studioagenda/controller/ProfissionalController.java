package com.salao.studioagenda.controller;

import com.salao.studioagenda.dto.ProfissionalRequest;
import com.salao.studioagenda.dto.ProfissionalResponse;
import com.salao.studioagenda.exception.RegraNegocioException;
import com.salao.studioagenda.service.ProfissionalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profissionais")
@RequiredArgsConstructor
public class ProfissionalController {

    private final ProfissionalService profissionalService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("profissionais", profissionalService.listarTodos());
        return "profissionais/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("profissional", new ProfissionalRequest(null, null));
        return "profissionais/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("profissional") ProfissionalRequest profissional, BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "profissionais/form";
        }
        profissionalService.criar(profissional);
        redirectAttributes.addFlashAttribute("sucesso", "Profissional cadastrado com sucesso");
        return "redirect:/profissionais";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        ProfissionalResponse profissional = profissionalService.buscarPorId(id);
        model.addAttribute("profissionalId", id);
        model.addAttribute("profissional", new ProfissionalRequest(profissional.nome(), profissional.especialidade()));
        return "profissionais/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("profissional") ProfissionalRequest profissional,
                            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("profissionalId", id);
            return "profissionais/form";
        }
        profissionalService.atualizar(id, profissional);
        redirectAttributes.addFlashAttribute("sucesso", "Profissional atualizado com sucesso");
        return "redirect:/profissionais";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            profissionalService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Profissional excluído com sucesso");
        } catch (RegraNegocioException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/profissionais";
    }
}
