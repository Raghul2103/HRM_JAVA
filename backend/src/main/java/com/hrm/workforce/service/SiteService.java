package com.hrm.workforce.service;

import com.hrm.workforce.dto.SiteDto;
import java.util.List;

public interface SiteService {
    SiteDto createSite(SiteDto dto);
    SiteDto updateSite(Long id, SiteDto dto);
    List<SiteDto> getAllSites();
    SiteDto getSiteById(Long id);
    void deleteSite(Long id);
}
