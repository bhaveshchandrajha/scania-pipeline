/**
 * Application service implementing warranty claim business logic.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1983}.
 */

package com.scania.warranty.service;

import com.scania.warranty.domain.Hsahkpf;
import com.scania.warranty.domain.Hsepaf;
import com.scania.warranty.domain.V4CheckCriteria;
import com.scania.warranty.repository.HsahkpfRepository;
import com.scania.warranty.repository.HsepafRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class V4ValidationService {

    private final HsahkpfRepository hsahkpfRepository;
    private final HsepafRepository hsepafRepository;

    public V4ValidationService(HsahkpfRepository hsahkpfRepository,
                               HsepafRepository hsepafRepository) {
        this.hsahkpfRepository = hsahkpfRepository;
        this.hsepafRepository = hsepafRepository;
    }

    public boolean checkV4(V4CheckCriteria criteria) {
        try { // @rpg-trace: n1985
            if (criteria.isV4Code()) { // @rpg-trace: n1987
                return false; // @rpg-trace: n1989
            } // @rpg-trace: n1986

            String v4Code = criteria.extractV4Code(); // @rpg-trace: n1992
            if (v4Code == null) { // @rpg-trace: n1992
                return false; // @rpg-trace: n2008
            } // @rpg-trace: n1992

            Optional<Hsahkpf> hsahkpfRecord = hsahkpfRepository.findByCompositeKey( // @rpg-trace: n1991
                criteria.g71000(), // @rpg-trace: n1991
                criteria.g71010(), // @rpg-trace: n1991
                criteria.g71020(), // @rpg-trace: n1991
                " ", // @rpg-trace: n1991
                criteria.g71030(), // @rpg-trace: n1991
                criteria.g71040(), // @rpg-trace: n1991
                criteria.g71190(), // @rpg-trace: n1991
                v4Code // @rpg-trace: n1991
            ); // @rpg-trace: n1991

            if (hsahkpfRecord.isPresent()) { // @rpg-trace: n1994
                Hsahkpf ahkRecord = hsahkpfRecord.get(); // @rpg-trace: n1995

                List<Hsepaf> hsepafRecords = hsepafRepository.findByCompositeKeyPrefix( // @rpg-trace: n1996
                    ahkRecord.getAhk000(), // @rpg-trace: n1996
                    ahkRecord.getAhk040(), // @rpg-trace: n1996
                    ahkRecord.getAhk050(), // @rpg-trace: n1996
                    ahkRecord.getAhk060(), // @rpg-trace: n1996
                    "V4" // @rpg-trace: n1996
                ); // @rpg-trace: n1996

                for (Hsepaf epaRecord : hsepafRecords) { // @rpg-trace: n1997
                    if (epaRecord.getEpaDatv() != null && // @rpg-trace: n2001
                        epaRecord.getEpaDatv().equals(ahkRecord.getAhk080())) { // @rpg-trace: n2001
                        return true; // @rpg-trace: n2002
                    } // @rpg-trace: n2000
                } // @rpg-trace: n1997
            } // @rpg-trace: n1993

        } catch (Exception e) { // @rpg-trace: n2006
            return false; // @rpg-trace: n2008
        } // @rpg-trace: n1985

        return false; // @rpg-trace: n2008
    }
}