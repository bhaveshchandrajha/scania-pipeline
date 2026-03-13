/**
 * Data transfer object for API or display.
 * <p>
 * Generated from RPG: unit {@code HS1210}, node {@code n1779}.
 */

package com.scania.warranty.dto;

public record ClaimCreationRequest(
    String kuerzel, // @rpg-trace: n1779
    String claimNr, // @rpg-trace: n1779
    String pakz, // @rpg-trace: n1779
    String rechNr, // @rpg-trace: n1779
    String rechDatum, // @rpg-trace: n1779
    String auftragsNr, // @rpg-trace: n1779
    String bereich // @rpg-trace: n1779
) {
}