package pf.cyj.sys.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pf.cyj.sys.dto.request.CmnCdGrpSaveReq;
import pf.cyj.sys.dto.request.CmnCdSaveReq;
import pf.cyj.sys.dto.response.CmnCdGrpRsp;
import pf.cyj.sys.dto.response.CmnCdRsp;
import pf.cyj.sys.entity.CmnCd;
import pf.cyj.sys.entity.CmnCdGrp;
import pf.cyj.sys.entity.CmnCdId;
import pf.cyj.sys.repository.CmnCdGrpRepository;
import pf.cyj.sys.repository.CmnCdRepository;

/** 공통코드 - 그룹/상세 등록·수정(upsert), 조회 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmnCdService {

    private final CmnCdGrpRepository cmnCdGrpRepository;
    private final CmnCdRepository cmnCdRepository;

    @Transactional
    public CmnCdGrpRsp saveGrp(CmnCdGrpSaveReq req) {
        CmnCdGrp grp = cmnCdGrpRepository.findById(req.codeGroup())
                .map(existing -> {
                    existing.setGroupName(req.groupName());
                    return existing;
                })
                .orElseGet(() -> CmnCdGrp.builder()
                        .codeGroup(req.codeGroup())
                        .groupName(req.groupName())
                        .build());

        return CmnCdGrpRsp.from(cmnCdGrpRepository.save(grp));
    }

    public List<CmnCdGrpRsp> findAllGrp() {
        return cmnCdGrpRepository.findAll().stream().map(CmnCdGrpRsp::from).toList();
    }

    @Transactional
    public CmnCdRsp saveCd(CmnCdSaveReq req) {
        CmnCdId id = new CmnCdId(req.codeGroup(), req.codeValue());

        CmnCd cd = cmnCdRepository.findById(id)
                .map(existing -> {
                    existing.setCodeName(req.codeName());
                    existing.setSortOrder(req.sortOrder());
                    existing.setUseYn(Boolean.TRUE.equals(req.useYn()));
                    return existing;
                })
                .orElseGet(() -> CmnCd.builder()
                        .codeGroup(req.codeGroup())
                        .codeValue(req.codeValue())
                        .codeName(req.codeName())
                        .sortOrder(req.sortOrder())
                        .useYn(Boolean.TRUE.equals(req.useYn()))
                        .build());

        return CmnCdRsp.from(cmnCdRepository.save(cd));
    }

    public List<CmnCdRsp> findCdByGrp(String codeGroup, boolean useOnly) {
        List<CmnCd> list = useOnly
                ? cmnCdRepository.findByCodeGroupAndUseYnTrueOrderBySortOrderAsc(codeGroup)
                : cmnCdRepository.findByCodeGroupOrderBySortOrderAsc(codeGroup);

        return list.stream().map(CmnCdRsp::from).toList();
    }
}
