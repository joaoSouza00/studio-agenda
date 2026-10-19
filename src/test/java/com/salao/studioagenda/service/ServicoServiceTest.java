package com.salao.studioagenda.service;

import com.salao.studioagenda.dto.ServicoRequest;
import com.salao.studioagenda.dto.ServicoResponse;
import com.salao.studioagenda.exception.RegraNegocioException;
import com.salao.studioagenda.model.Servico;
import com.salao.studioagenda.repository.AgendamentoRepository;
import com.salao.studioagenda.repository.ServicoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServicoServiceTest {

    @Mock
    private ServicoRepository servicoRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private ServicoService servicoService;

    @Test
    void criarDeveSalvarDuracaoEPreco() {
        when(servicoRepository.save(any(Servico.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServicoResponse response = servicoService.criar(new ServicoRequest("Corte", 30, new BigDecimal("50.00")));

        assertThat(response.duracaoMinutos()).isEqualTo(30);
        assertThat(response.preco()).isEqualByComparingTo("50.00");
    }

    @Test
    void atualizarDeveAlterarServicoExistente() {
        Servico servico = criarServico();
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(servicoRepository.save(servico)).thenReturn(servico);

        ServicoResponse response = servicoService.atualizar(1L, new ServicoRequest("Corte masculino", 40, new BigDecimal("60.00")));

        assertThat(response.nome()).isEqualTo("Corte masculino");
        assertThat(response.duracaoMinutos()).isEqualTo(40);
        assertThat(response.preco()).isEqualByComparingTo("60.00");
    }

    @Test
    void excluirDeveBloquearServicoVinculadoAAgendamentos() {
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(criarServico()));
        when(agendamentoRepository.existsByServicosId(1L)).thenReturn(true);

        assertThatThrownBy(() -> servicoService.excluir(1L))
                .isInstanceOf(RegraNegocioException.class);
        verify(servicoRepository, never()).delete(any());
    }

    @Test
    void excluirDeveRemoverServicoSemAgendamentos() {
        Servico servico = criarServico();
        when(servicoRepository.findById(1L)).thenReturn(Optional.of(servico));
        when(agendamentoRepository.existsByServicosId(1L)).thenReturn(false);

        servicoService.excluir(1L);

        verify(servicoRepository).delete(servico);
    }

    private Servico criarServico() {
        Servico servico = new Servico();
        servico.setId(1L);
        servico.setNome("Corte");
        servico.setDuracaoMinutos(30);
        servico.setPreco(new BigDecimal("50.00"));
        return servico;
    }
}
