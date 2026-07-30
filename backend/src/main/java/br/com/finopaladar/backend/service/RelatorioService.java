package br.com.finopaladar.backend.service;

import br.com.finopaladar.backend.dto.CidadeMaisPedidosResponse;
import br.com.finopaladar.backend.dto.ProdutoVendidoResponse;
import br.com.finopaladar.backend.dto.RelatorioFaturamentoResponse;
import br.com.finopaladar.backend.dto.TicketMedioResponse;
import br.com.finopaladar.backend.entity.StatusPedido;
import br.com.finopaladar.backend.exception.BusinessException;
import br.com.finopaladar.backend.repository.RelatorioRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RelatorioService {

    private static final int ESCALA_MONETARIA = 2;
    private static final StatusPedido STATUS_FATURAMENTO = StatusPedido.ENTREGUE;

    private final RelatorioRepository relatorioRepository;

    public RelatorioService(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    @Transactional(readOnly = true)
    public RelatorioFaturamentoResponse faturamentoDiario(LocalDate data) {
        validarData(data);
        return faturamentoPorPeriodo(data, data);
    }

    @Transactional(readOnly = true)
    public RelatorioFaturamentoResponse faturamentoSemanal(LocalDate dataReferencia) {
        validarData(dataReferencia);
        LocalDate dataInicial = dataReferencia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate dataFinal = dataReferencia.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return faturamentoPorPeriodo(dataInicial, dataFinal);
    }

    @Transactional(readOnly = true)
    public RelatorioFaturamentoResponse faturamentoMensal(Integer ano, Integer mes) {
        validarAno(ano);
        validarMes(mes);
        YearMonth periodo = YearMonth.of(ano, mes);
        return faturamentoPorPeriodo(periodo.atDay(1), periodo.atEndOfMonth());
    }

    @Transactional(readOnly = true)
    public RelatorioFaturamentoResponse faturamentoAnual(Integer ano) {
        validarAno(ano);
        return faturamentoPorPeriodo(LocalDate.of(ano, 1, 1), LocalDate.of(ano, 12, 31));
    }

    @Transactional(readOnly = true)
    public RelatorioFaturamentoResponse faturamentoPorPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        validarPeriodo(dataInicial, dataFinal);
        BigDecimal valorTotal = relatorioRepository.sumValorTotalByStatusAndPeriodo(
                STATUS_FATURAMENTO,
                dataInicial,
                dataFinal
        );

        return new RelatorioFaturamentoResponse(dataInicial, dataFinal, valorOuZero(valorTotal));
    }

    @Transactional(readOnly = true)
    public List<ProdutoVendidoResponse> produtosMaisVendidos(
            LocalDate dataInicial,
            LocalDate dataFinal,
            Integer limite
    ) {
        validarPeriodo(dataInicial, dataFinal);
        validarLimite(limite);
        return relatorioRepository.findProdutosMaisVendidosByStatusAndPeriodo(
                STATUS_FATURAMENTO,
                dataInicial,
                dataFinal,
                PageRequest.of(0, limite)
        );
    }

    @Transactional(readOnly = true)
    public List<ProdutoVendidoResponse> quantidadeVendidaPorProduto(LocalDate dataInicial, LocalDate dataFinal) {
        validarPeriodo(dataInicial, dataFinal);
        return relatorioRepository.findQuantidadeVendidaPorProdutoByStatusAndPeriodo(
                STATUS_FATURAMENTO,
                dataInicial,
                dataFinal
        );
    }

    @Transactional(readOnly = true)
    public TicketMedioResponse ticketMedio(LocalDate dataInicial, LocalDate dataFinal) {
        validarPeriodo(dataInicial, dataFinal);
        BigDecimal valorTotal = valorOuZero(relatorioRepository.sumValorTotalByStatusAndPeriodo(
                STATUS_FATURAMENTO,
                dataInicial,
                dataFinal
        ));
        long quantidadePedidos = relatorioRepository.countPedidosByStatusAndPeriodo(
                STATUS_FATURAMENTO,
                dataInicial,
                dataFinal
        );

        if (quantidadePedidos == 0) {
            return new TicketMedioResponse(dataInicial, dataFinal, BigDecimal.ZERO);
        }

        BigDecimal valor = valorTotal.divide(
                BigDecimal.valueOf(quantidadePedidos),
                ESCALA_MONETARIA,
                RoundingMode.HALF_UP
        );
        return new TicketMedioResponse(dataInicial, dataFinal, valor);
    }

    @Transactional(readOnly = true)
    public CidadeMaisPedidosResponse cidadeComMaisPedidos(LocalDate dataInicial, LocalDate dataFinal) {
        validarPeriodo(dataInicial, dataFinal);
        return relatorioRepository.findCidadesComMaisPedidosByStatusAndPeriodo(
                        STATUS_FATURAMENTO,
                        dataInicial,
                        dataFinal,
                        PageRequest.of(0, 1)
                )
                .stream()
                .findFirst()
                .orElse(new CidadeMaisPedidosResponse(null, 0L));
    }

    private void validarPeriodo(LocalDate dataInicial, LocalDate dataFinal) {
        if (dataInicial == null || dataFinal == null) {
            throw new BusinessException("Periodo deve ser informado");
        }
        if (dataFinal.isBefore(dataInicial)) {
            throw new BusinessException("Data final deve ser maior ou igual a data inicial");
        }
    }

    private void validarData(LocalDate data) {
        if (data == null) {
            throw new BusinessException("Data deve ser informada");
        }
    }

    private void validarAno(Integer ano) {
        if (ano == null || ano < 1) {
            throw new BusinessException("Ano deve ser informado");
        }
    }

    private void validarMes(Integer mes) {
        if (mes == null || mes < 1 || mes > 12) {
            throw new BusinessException("Mes deve estar entre 1 e 12");
        }
    }

    private void validarLimite(Integer limite) {
        if (limite == null || limite < 1) {
            throw new BusinessException("Limite deve ser maior que zero");
        }
    }

    private BigDecimal valorOuZero(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
