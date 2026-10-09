package com.salao.studioagenda.repository;

import com.salao.studioagenda.model.Agendamento;
import com.salao.studioagenda.model.StatusAgendamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface AgendamentoRepository extends JpaRepository<Agendamento, Long> {

    @Query("""
            SELECT a FROM Agendamento a
            WHERE a.profissional.id = :profissionalId
            AND a.status <> :statusCancelado
            AND (:agendamentoId IS NULL OR a.id <> :agendamentoId)
            AND a.dataHoraInicio < :dataHoraFim
            AND a.dataHoraFim > :dataHoraInicio
            """)
    List<Agendamento> buscarConflitosDeHorario(
            @Param("profissionalId") Long profissionalId,
            @Param("dataHoraInicio") LocalDateTime dataHoraInicio,
            @Param("dataHoraFim") LocalDateTime dataHoraFim,
            @Param("agendamentoId") Long agendamentoId,
            @Param("statusCancelado") StatusAgendamento statusCancelado
    );

    List<Agendamento> findByClienteId(Long clienteId);

    List<Agendamento> findByProfissionalId(Long profissionalId);

    boolean existsByClienteId(Long clienteId);

    boolean existsByProfissionalId(Long profissionalId);

    boolean existsByServicosId(Long servicoId);
}
