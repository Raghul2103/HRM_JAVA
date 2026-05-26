package com.hrm.workforce.service.impl;

import com.hrm.workforce.dto.SiteDto;
import com.hrm.workforce.entity.Site;
import com.hrm.workforce.exception.ResourceNotFoundException;
import com.hrm.workforce.repository.SiteRepository;
import com.hrm.workforce.service.SiteService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;

    public SiteServiceImpl(SiteRepository siteRepository) {
        this.siteRepository = siteRepository;
    }

    @Override
    @Transactional
    public SiteDto createSite(SiteDto dto) {
        Site site = Site.builder()
                .siteName(dto.getSiteName())
                .location(dto.getLocation())
                .active(dto.isActive())
                .build();
        site = siteRepository.save(site);
        return mapToDto(site);
    }

    @Override
    @Transactional
    public SiteDto updateSite(Long id, SiteDto dto) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with ID: " + id));

        site.setSiteName(dto.getSiteName());
        site.setLocation(dto.getLocation());
        site.setActive(dto.isActive());

        site = siteRepository.save(site);
        return mapToDto(site);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SiteDto> getAllSites() {
        return siteRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public SiteDto getSiteById(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with ID: " + id));
        return mapToDto(site);
    }

    @Override
    @Transactional
    public void deleteSite(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Site not found with ID: " + id));
        site.setActive(false);
        siteRepository.save(site);
    }

    private SiteDto mapToDto(Site site) {
        return SiteDto.builder()
                .id(site.getId())
                .siteName(site.getSiteName())
                .location(site.getLocation())
                .active(site.isActive())
                .build();
    }
}
