package com.salao.studioagenda.controller;

import com.salao.studioagenda.dto.ServicoRequest;
import com.salao.studioagenda.dto.ServicoResponse;
import com.salao.studioagenda.exception.RegraNegocioException;
import com.salao.studioagenda.service.ServicoService;
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
@RequestMapping("/servicos")
@RequiredArgsConstructor
public class ServicoController {

    private final ServicoService servicoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("servicos", servicoService.listarTodos());
        return "servicos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("servico", new ServicoRequest(null, null, null));
        return "servicos/form";
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("servico") ServicoRequest servico, BindingResult result,
                        RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "servicos/form";
        }
        servicoService.criar(servico);
        redirectAttributes.addFlashAttribute("sucesso", "Serviço cadastrado com sucesso");
        return "redirect:/servicos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model) {
        ServicoResponse servico = servicoService.buscarPorId(id);
        model.addAttribute("servicoId", id);
        model.addAttribute("servico", new ServicoRequest(servico.nome(), servico.duracaoMinutos(), servico.preco()));
        return "servicos/form";
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("servico") ServicoRequest servico,
                            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("servicoId", id);
            return "servicos/form";
        }
        servicoService.atualizar(id, servico);
        redirectAttributes.addFlashAttribute("sucesso", "Serviço atualizado com sucesso");
        return "redirect:/servicos";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            servicoService.excluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Serviço excluído com sucesso");
        } catch (RegraNegocioException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/servicos";
    }
}
