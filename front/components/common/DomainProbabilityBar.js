import React from "react";
import PropTypes from "prop-types";
import { Progress, Tag } from "antd";

// DOMAIN_PREDICTION.PROBABILITY(0~1)를 antd Progress 로 시각화한다.
// 1순위는 파란색으로 강조하고, 그 아래 순위는 회색조로 톤을 낮춘다.
function DomainProbabilityBar({ rank, domainNameKo, probability = 0, cacheHitYn = false }) {
  const percent = Math.round((probability || 0) * 100);
  const isTop = rank === 1;

  return (
    <div style={{ marginBottom: 8 }}>
      <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 2 }}>
        <span>
          <Tag color={isTop ? "blue" : "default"}>
            {rank}
            순위
          </Tag>
          {domainNameKo}
          {cacheHitYn && (
            <Tag color="green" style={{ marginLeft: 6 }}>캐시</Tag>
          )}
        </span>
        <span>
          {percent}
          %
        </span>
      </div>
      <Progress
        percent={percent}
        showInfo={false}
        strokeColor={isTop ? "#1677ff" : "#bfbfbf"}
        size="small"
      />
    </div>
  );
}

DomainProbabilityBar.propTypes = {
  rank: PropTypes.number.isRequired,
  domainNameKo: PropTypes.string.isRequired,
  probability: PropTypes.number,
  cacheHitYn: PropTypes.bool,
};

export default DomainProbabilityBar;
