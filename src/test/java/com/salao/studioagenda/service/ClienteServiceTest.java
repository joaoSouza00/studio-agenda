package com.salao.studioagenda.service;

import com.salao.studioagenda.dto.ClienteRequest;
import com.salao.studioagenda.dto.ClienteResponse;
import com.salao.studioagenda.exception.RecursoNaoEncontradoException;
import com.salao.studioagenda.exception.RegraNegocioException;
import com.salao.studioagenda.model.Cliente;
import com.salao.studioagenda.repository.AgendamentoRepository;
import com.salao.studioagenda.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AgendamentoRepository agendamentoRepository;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void listarTodosDeveConverterParaResponse() {
        when(clienteRepository.findAll()).thenReturn(List.of(criarCliente(1L, "Maria"), criarCliente(2L, "João")));

        List<ClienteResponse> clientes = clienteService.listarTodos();

        assertThat(clientes).extracting(ClienteResponse::nome).containsExactly("Maria", "João");
    }

    @Test
    void criarDeveSalvarDadosDoRequest() {
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ClienteResponse response = clienteService.criar(new ClienteRequest("Maria", "11999990000", "maria@email.com"));

        ArgumentCaptor<Cliente> captor = ArgumentCaptor.forClass(Cliente.class);
        verify(clienteRepository).save(captor.capture());
        assertThat(captor.getValue().getNome()).isEqualTo("Maria");
        assertThat(captor.getValue().getTelefone()).isEqualTo("11999990000");
        assertThat(response.email()).isEqualTo("maria@email.com");
    }

    @Test
    void atualizarDeveAlterarClienteExistente() {
        Cliente cliente = criarCliente(1L, "Maria");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(clienteRepository.save(cliente)).thenReturn(cliente);

        ClienteResponse response = clienteService.atualizar(1L, new ClienteRequest("Maria Souza", "11888880000", null));

        assertThat(response.nome()).isEqualTo("Maria Souza");
        assertThat(response.telefone()).isEqualTo("11888880000");
    }

    @Test
    void buscarPorIdDeveLancarExcecaoQuandoNaoExistir() {
        when(clienteRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> clienteService.buscarPorId(99L))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    void excluirDeveRemoverClienteSemAgendamentos() {
        Cliente cliente = criarCliente(1L, "Maria");
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(agendamentoRepository.existsByClienteId(1L)).thenReturn(false);

        clienteService.excluir(1L);

        verify(clienteRepository).delete(cliente);
    }

    @Test
    void excluirDeveBloquearClienteComAgendamentos() {
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(criarCliente(1L, "Maria")));
        when(agendamentoRepository.existsByClienteId(1L)).thenReturn(true);

        assertThatThrownBy(() -> clienteService.excluir(1L))
                .isInstanceOf(RegraNegocioException.class);
        verify(clienteRepository, never()).delete(any());
    }

    private Cliente criarCliente(Long id, String nome) {
        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setNome(nome);
        cliente.setTelefone("11999990000");
        return cliente;
    }
}
