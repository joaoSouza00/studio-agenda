package com.salao.studioagenda.service;

import com.salao.studioagenda.dto.ProfissionalRequest;
import com.salao.studioagenda.dto.ProfissionalResponse;
import com.salao.studioagenda.exception.RecursoNaoEncontradoException;
import com.salao.studioagenda.exception.RegraNegocioException;
import com.salao.studioagenda.model.Profissional;
import com.salao.studioagenda.repository.AgendamentoRepository;
import com.salao.studioagenda.repository.ProfissionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfissionalServiceTest {

    @Mock
    private ProfissionalRepository profissionalRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private ProfissionalService profissionalService;

    @Test
    void criarDeveSalvarDadosDoRequest() {
        when(profissionalRepository.save(any(Profissional.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ProfissionalResponse response = profissionalService.criar(new ProfissionalRequest("Ana", "Cabeleireira"));

        assertThat(response.nome()).isEqualTo("Ana");
        assertThat(response.especialidade()).isEqualTo("Cabeleireira");
    }

    @Test
    void atualizarDeveLancarExcecaoQuandoNaoExistir() {
        when(profissionalRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> profissionalService.atualizar(99L, new ProfissionalRequest("Ana", "Manicure")))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(profissionalRepository, never()).save(any());
    }

    @Test
    void excluirDeveBloquearProfissionalComAgendamentos() {
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(criarProfissional()));
        when(agendamentoRepository.existsByProfissionalId(1L)).thenReturn(true);

        assertThatThrownBy(() -> profissionalService.excluir(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("Ana");
        verify(profissionalRepository, never()).delete(any());
    }

    @Test
    void excluirDeveRemoverProfissionalSemAgendamentos() {
        Profissional profissional = criarProfissional();
        when(profissionalRepository.findById(1L)).thenReturn(Optional.of(profissional));
        when(agendamentoRepository.existsByProfissionalId(1L)).thenReturn(false);

        profissionalService.excluir(1L);

        verify(profissionalRepository).delete(profissional);
    }

    private Profissional criarProfissional() {
        Profissional profissional = new Profissional();
        profissional.setId(1L);
        profissional.setNome("Ana");
        profissional.setEspecialidade("Cabeleireira");
        return profissional;
    }
}
