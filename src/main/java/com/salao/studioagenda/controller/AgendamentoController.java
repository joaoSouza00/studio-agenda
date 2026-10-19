package com.salao.studioagenda.controller;

import com.salao.studioagenda.dto.AgendamentoRequest;
import com.salao.studioagenda.dto.AgendamentoResponse;
import com.salao.studioagenda.dto.ServicoResponse;
import com.salao.studioagenda.exception.ConflitoHorarioException;
import com.salao.studioagenda.exception.RegraNegocioException;
import com.salao.studioagenda.model.StatusAgendamento;
import com.salao.studioagenda.service.AgendamentoService;
import com.salao.studioagenda.service.ClienteService;
import com.salao.studioagenda.service.ProfissionalService;
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

import java.util.stream.Collectors;

@Controller
@RequestMapping("/agendamentos")
@RequiredArgsConstructor
public class AgendamentoController {

    private final AgendamentoService agendamentoService;
    private final ClienteService clienteService;
    private final ProfissionalService profissionalService;
    private final ServicoService servicoService;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("agendamentos", agendamentoService.listarTodos());
        return "agendamentos/lista";
    }

    @GetMapping("/novo")
    public String novo(Model model) {
        model.addAttribute("agendamento", new AgendamentoRequest(null, null, null, null));
        return exibirFormulario(model, null);
    }

    @PostMapping
    public String criar(@Valid @ModelAttribute("agendamento") AgendamentoRequest agendamento, BindingResult result,
                        Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return exibirFormulario(model, null);
        }
        try {
            agendamentoService.criar(agendamento);
        } catch (ConflitoHorarioException ex) {
            model.addAttribute("erro", ex.getMessage());
            return exibirFormulario(model, null);
        }
        redirectAttributes.addFlashAttribute("sucesso", "Agendamento criado com sucesso");
        return "redirect:/agendamentos";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        AgendamentoResponse agendamento = agendamentoService.buscarPorId(id);
        if (agendamento.status() != StatusAgendamento.AGENDADO) {
            redirectAttributes.addFlashAttribute("erro", "Apenas agendamentos com status AGENDADO podem ser editados");
            return "redirect:/agendamentos";
        }
        model.addAttribute("agendamento", new AgendamentoRequest(
                agendamento.clienteId(),
                agendamento.profissionalId(),
                agendamento.servicos().stream().map(ServicoResponse::id).collect(Collectors.toSet()),
                agendamento.dataHoraInicio()
        ));
        return exibirFormulario(model, id);
    }

    @PostMapping("/{id}")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("agendamento") AgendamentoRequest agendamento,
                            BindingResult result, Model model, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return exibirFormulario(model, id);
        }
        try {
            agendamentoService.atualizar(id, agendamento);
        } catch (ConflitoHorarioException | RegraNegocioException ex) {
            model.addAttribute("erro", ex.getMessage());
            return exibirFormulario(model, id);
        }
        redirectAttributes.addFlashAttribute("sucesso", "Agendamento atualizado com sucesso");
        return "redirect:/agendamentos";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.cancelar(id);
            redirectAttributes.addFlashAttribute("sucesso", "Agendamento cancelado com sucesso");
        } catch (RegraNegocioException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/agendamentos";
    }

    @PostMapping("/{id}/concluir")
    public String concluir(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            agendamentoService.concluir(id);
            redirectAttributes.addFlashAttribute("sucesso", "Agendamento concluído com sucesso");
        } catch (RegraNegocioException ex) {
            redirectAttributes.addFlashAttribute("erro", ex.getMessage());
        }
        return "redirect:/agendamentos";
    }

    private String exibirFormulario(Model model, Long agendamentoId) {
        model.addAttribute("agendamentoId", agendamentoId);
        model.addAttribute("clientes", clienteService.listarTodos());
        model.addAttribute("profissionais", profissionalService.listarTodos());
        model.addAttribute("servicos", servicoService.listarTodos());
        return "agendamentos/form";
    }
}
