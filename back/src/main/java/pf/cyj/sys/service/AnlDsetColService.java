package pf.cyj.sys.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import pf.cyj.sys.dto.request.AnlColBulkCreateReq;
import pf.cyj.sys.dto.request.AnlColCreateReq;
import pf.cyj.sys.dto.request.AnlDsetCreateReq;
import pf.cyj.sys.dto.response.AnlColRsp;
import pf.cyj.sys.dto.response.AnlDsetRsp;
import pf.cyj.sys.entity.AnlCol;
import pf.cyj.sys.entity.AnlDset;
import pf.cyj.sys.entity.AppUsr;
import pf.cyj.sys.exception.ResourceNotFoundException;
import pf.cyj.sys.repository.AnlColRepository;
import pf.cyj.sys.repository.AnlDsetRepository;
import pf.cyj.sys.repository.AppUsrRepository;
import pf.cyj.sys.util.BizIdGenerator;

/** 분석대상 - 데이터셋 등록(업무키 채번 포함), 컬럼 메타 일괄 등록 및 조회 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnlDsetColService {

    private final AppUsrRepository appUsrRepository;
    private final AnlDsetRepository anlDsetRepository;
    private final AnlColRepository anlColRepository;
    private final BizIdGenerator bizIdGenerator;

    @Transactional
    public AnlDsetRsp createDset(AnlDsetCreateReq req, Long requesterId) {
        AppUsr requester = appUsrRepository.findById(requesterId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 사용자입니다: " + requesterId));

        AnlDset dset = AnlDset.builder()
                .datasetId(bizIdGenerator.nextDatasetId())
                .requestNo(bizIdGenerator.nextRequestNo())
                .datasetName(req.datasetName())
                .dbSchemaName(req.dbSchemaName())
                .tableName(req.tableName())
                .requestedBy(requester)
                .build();

        if (req.dbmsTypeCode() != null && !req.dbmsTypeCode().isBlank()) {
            dset.setDbmsTypeCode(req.dbmsTypeCode());
        }

        return AnlDsetRsp.from(anlDsetRepository.save(dset));
    }

    public AnlDsetRsp findDsetById(String datasetId) {
        return anlDsetRepository.findById(datasetId)
                .map(AnlDsetRsp::from)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 데이터셋입니다: " + datasetId));
    }

    public List<AnlDsetRsp> findDsetByRequester(Long requesterId) {
        return anlDsetRepository.findByRequestedBy_UserId(requesterId).stream().map(AnlDsetRsp::from).toList();
    }

    @Transactional
    public List<AnlColRsp> createColumnsBulk(AnlColBulkCreateReq req) {
        AnlDset dset = anlDsetRepository.findById(req.datasetId())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 데이터셋입니다: " + req.datasetId()));

        List<AnlCol> columns = req.columns().stream()
                .map(c -> toEntity(dset, c))
                .toList();

        return anlColRepository.saveAll(columns).stream().map(AnlColRsp::from).toList();
    }

    public List<AnlColRsp> findColumnsByDataset(String datasetId) {
        return anlColRepository.findByAnlDset_DatasetId(datasetId).stream().map(AnlColRsp::from).toList();
    }

    private AnlCol toEntity(AnlDset dset, AnlColCreateReq c) {
        return AnlCol.builder()
                .anlDset(dset)
                .columnName(c.columnName())
                .columnNameKo(c.columnNameKo())
                .columnNameEn(c.columnNameEn())
                .dataType(c.dataType())
                .dataLength(c.dataLength())
                .dataScale(c.dataScale())
                .numericYn(Boolean.TRUE.equals(c.numericYn()))
                .dateYn(Boolean.TRUE.equals(c.dateYn()))
                .uniqueYn(Boolean.TRUE.equals(c.uniqueYn()))
                .build();
    }
}
