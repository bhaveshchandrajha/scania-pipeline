/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n404}.
 */

package com.scania.warranty.dto;

public record ClaimSubfileDto(
    String pakz,
    String rechNr,
    String rechDatum,
    String auftragsNr,
    String claimNr,
    String chassisNr,
    String kennzeichen,
    String repDatum,
    Integer kmStand,
    String kdNr,
    String kdName,
    String claimNrSde,
    Integer statusCodeSde,
    String statusText,
    boolean colorRed,
    boolean colorYellow,
    boolean colorBlue,
    String colorCode,
    int failureCount
) {
}