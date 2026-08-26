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

    /** 공통코드 그룹을 등록하거나, 이미 존재하면 그룹명만 갱신한다(upsert). */
    @Transactional
    public CmnCdGrpRsp saveGrp(CmnCdGrpSaveReq req) {
        CmnCdGrp grp = cmnCdGrpRepository.findById(req.getCodeGroup())
                .map(existing -> {
                    existing.setGroupName(req.getGroupName());
                    return existing;
                })
                .orElseGet(() -> CmnCdGrp.builder()
                        .codeGroup(req.getCodeGroup())
                        .groupName(req.getGroupName())
                        .build());

        return CmnCdGrpRsp.from(cmnCdGrpRepository.save(grp));
    }

    /** 등록된 모든 공통코드 그룹을 조회한다. */
    public List<CmnCdGrpRsp> findAllGrp() {
        return cmnCdGrpRepository.findAll().stream().map(CmnCdGrpRsp::from).toList();
    }

    /** 공통코드 상세를 등록하거나, (codeGroup, codeValue) 가 이미 존재하면 나머지 값만 갱신한다(upsert). */
    @Transactional
    public CmnCdRsp saveCd(CmnCdSaveReq req) {
        CmnCdId id = new CmnCdId(req.getCodeGroup(), req.getCodeValue());

        CmnCd cd = cmnCdRepository.findById(id)
                .map(existing -> {
                    existing.setCodeName(req.getCodeName());
                    existing.setSortOrder(req.getSortOrder());
                    existing.setUseYn(Boolean.TRUE.equals(req.getUseYn()));
                    return existing;
                })
                .orElseGet(() -> CmnCd.builder()
                        .codeGroup(req.getCodeGroup())
                        .codeValue(req.getCodeValue())
                        .codeName(req.getCodeName())
                        .sortOrder(req.getSortOrder())
                        .useYn(Boolean.TRUE.equals(req.getUseYn()))
                        .build());

        return CmnCdRsp.from(cmnCdRepository.save(cd));
    }

    /** 그룹별 공통코드 상세를 정렬순서 오름차순으로 조회한다. useOnly=true 면 사용중(useYn=true)인 코드만 걸러서 반환한다. */
    public List<CmnCdRsp> findCdByGrp(String codeGroup, boolean useOnly) {
        List<CmnCd> list = useOnly
                ? cmnCdRepository.findByCodeGroupAndUseYnTrueOrderBySortOrderAsc(codeGroup)
                : cmnCdRepository.findByCodeGroupOrderBySortOrderAsc(codeGroup);

        return list.stream().map(CmnCdRsp::from).toList();
    }
}
