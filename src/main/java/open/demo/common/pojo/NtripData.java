package open.demo.common.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NtripData {
    private Integer messageType;
    private Integer satelliteId;
    private Double orbitCorrection;
    private Double clockCorrection;
    private Double phaseCorrection;
    private Double atmosphericCorrection;

}