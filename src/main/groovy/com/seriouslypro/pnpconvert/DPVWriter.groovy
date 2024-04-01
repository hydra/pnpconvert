package com.seriouslypro.pnpconvert

import com.seriouslypro.pnpconvert.machine.Machine

import java.nio.charset.StandardCharsets
import java.text.DecimalFormat
import java.text.SimpleDateFormat

enum FormatVersion {
    // Firmware < 2725B
    LEGACY(
        "Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed".split(',')
    ),

    // Firmware >= 2725B
    LATEST(
        "Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,nPixSizeX,nPixSizeY,HeightTake,DelayTake,nPullStripSpeed,nThreshold,nVisualRadio".split(',')
    )

    String[] materialKeys

    FormatVersion(materialKeys) {
        this.materialKeys = materialKeys
    }
}

class DPVWriter {

    private String lineEnding
    private String tableLineEnding

    private PrintStream stream
    NumberSequence materialNumberSequence
    FormatVersion format = FormatVersion.LATEST

    // when enabled, disabled placements are created for each fiducial, allowing for nozzle calibration on latest firmware since the 'Head' button is broken in the latest firmware when doing calibration.
    boolean addPlacementsForFiducialsEnabled = false

    List<String[]> placements = []
    Map<Integer, Map<String, String>> materials = [:]
    List<String[]> trays = []

    OutputStream outputStream
    Machine machine
    BigDecimal offsetZ
    DPVHeader dpvHeader
    Optional<Panel> optionalPanel = Optional<Panel>.empty()
    Optional<List<Fiducial>> optionalFiducials = Optional<List<Fiducial>>.empty()

    public DPVWriter(
        OutputStream outputStream,
        Machine machine,
        BigDecimal offsetZ,
        DPVHeader dpvHeader
    ) {
        this.outputStream = outputStream
        this.machine = machine
        this.offsetZ = offsetZ
        this.dpvHeader = dpvHeader

        materialNumberSequence = new NumberSequence(0)
    }

    public assignMaterials(Map<ComponentPlacement, MaterialAssignment> materialAssignments) {
        placements = buildPlacements(machine, offsetZ, materialAssignments)

        materials = buildMaterials(machine, materialAssignments)
        trays = buildTrays(materialAssignments)

    }

    public setPanel(Optional<Panel> optionalPanel) {
        this.optionalPanel = optionalPanel
    }

    public setFiducials(Optional<List<Fiducial>> optionalFiducials) {
        this.optionalFiducials = optionalFiducials
    }

    public addMaterial(Integer feederId, Feeder feeder, Component component) {
        Map<String, String> material = buildMaterial(feederId, feeder, component)
        materials[feederId] = material
    }

    public write() {
        lineEnding = "\r\n"

        tableLineEnding = lineEnding * 2

        stream = new PrintStream(outputStream, false, StandardCharsets.UTF_8.toString())

        writeHeader(dpvHeader, optionalPanel)
        writeMaterials(materials)
        writePanel(optionalPanel)
        writePlacements(placements)
        writeTrays(trays)
        writeFiducials(optionalFiducials)
    }

    Map<Integer, Map<String, String>> buildMaterials(Machine machine, Map<ComponentPlacement, MaterialAssignment> materialAssignments) {
        Map<Integer, Map<String, String>> materials = materialAssignments.collectEntries { ComponentPlacement placement, MaterialAssignment materialAssignment ->
            Map<String, String> material = buildMaterial(materialAssignment.feederId, materialAssignment.feeder, materialAssignment.component)
            [materialAssignment.feederId, material]
        }
        materials
    }

    List<String[]> buildPlacements(Machine machine, BigDecimal offsetZ, Map<ComponentPlacement, MaterialAssignment> materialAssignments) {

        /*
        Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay
        EComponent,0,1,1,16,24.89,21.64,90,0.5,5,0,C1,100nF 6.3V 0402/CAP_0402,0
         */

        // The "Note" field is limited to 31 characters when re-saving using the machine, e.g. "47uF 6.3V 1206 10% TANTALUM/CAP_1206" will be re-saved by the machine as "47uF 6.3V 1206 10% TANTALUM/CAP".


        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        List<String[]> placements =[]

        NumberSequence placementNumberSequence = new NumberSequence(0)
        NumberSequence placementIDSequence = new NumberSequence(1)

        Integer firstFeederId = firstFeederId(materialAssignments)

        if (addPlacementsForFiducialsEnabled && firstFeederId) {
            optionalFiducials.ifPresent { fiducials ->
                fiducials.eachWithIndex { fiducial, index ->
                    String refdes = "FID${index + 1}"

                    String[] placement = [
                        "EComponent",
                        placementNumberSequence.next(),
                        placementIDSequence.next(),
                        "1", // PHead
                        firstFeederId, // STNo.
                        twoDigitDecimalFormat.format(fiducial.coordinate.x), // DeltX
                        twoDigitDecimalFormat.format(fiducial.coordinate.y), // DeltY
                        "0", // Angle
                        "0", // Height
                        "1", // Skip
                        "0", // Speed
                        refdes, // Explain
                        fiducial.note, // Note
                        "0", // Delay
                    ]
                    placements << placement
                }
            }
        }

        materialAssignments.each { ComponentPlacement componentPlacement, MaterialAssignment materialAssignment ->

            PickSettings pickSettings = materialAssignment.feeder.pickSettings
            FeederProperties feederProperties = machine.feederProperties(materialAssignment.feederId)

            BigDecimal counterClockwiseMachineAngle = calculateMachineAngle(
                componentPlacement.rotation,
                pickSettings.packageAngle,
                feederProperties.feederAngle
            )

            String[] placement = [
                "EComponent",
                placementNumberSequence.next(),
                placementIDSequence.next(),
                materialAssignment.feeder.pickSettings.head,
                materialAssignment.feederId,
                twoDigitDecimalFormat.format(componentPlacement.coordinate.x),
                twoDigitDecimalFormat.format(componentPlacement.coordinate.y),
                twoDigitDecimalFormat.format(counterClockwiseMachineAngle),
                twoDigitDecimalFormat.format(materialAssignment.component.height + offsetZ),
                buildStatus(componentPlacement.enabled && materialAssignment.feeder.enabled, pickSettings),
                buildPlaceSpeed(pickSettings.placeSpeedPercentage),
                componentPlacement.refdes,
                (materialAssignment.component.description + ";" + componentPlacement.value).take(31),
                (pickSettings.placeDelay * 100) as Integer
            ]

            placements << placement
        }

        return placements
    }

    private static Integer firstFeederId(Map<ComponentPlacement, MaterialAssignment> materialAssignments) {
        if (materialAssignments.size() == 0) {
            return null
        }
        materialAssignments.values().first().feederId
    }

    private int buildPlaceSpeed(int placeSpeedPercentage) {
        if (placeSpeedPercentage >= 100 || placeSpeedPercentage == 0) {
            return 100
        }
        return placeSpeedPercentage
    }

    public static BigDecimal calculateMachineAngle(BigDecimal designAngle, BigDecimal pickAngle, BigDecimal feederAngle) {

        BigDecimal machineAngle = (designAngle + feederAngle + pickAngle).remainder(360)

        machineAngle = 180 - machineAngle

        if (machineAngle > 180) machineAngle -= 360

        return machineAngle
    }

    List<String[]> buildTrays(Map<ComponentPlacement, MaterialAssignment> materialAssignments) {
        Map<Integer, MaterialAssignment> trays = [:]

        NumberSequence trayNumberSequence = new NumberSequence(0)

        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        materialAssignments.each { ComponentPlacement placement, MaterialAssignment materialAssignment ->
            Feeder candidate = materialAssignment.feeder

            boolean feederUsesTray = candidate instanceof TrayFeeder
            boolean alreadyProcessed = trays.containsKey(materialAssignment.feederId)
            if (!feederUsesTray || alreadyProcessed) {
                return
            }

            trays[materialAssignment.feederId] = materialAssignment
        }

        trays = trays.sort { a, b ->
            a.key <=> b.key
        }

        List<String[]> trayRows = trays.collect { Integer feederId, MaterialAssignment materialAssignment ->
            Tray tray = ((TrayFeeder) materialAssignment.feeder).tray

            String[] trayRow = [
                "ICTray",
                trayNumberSequence.next(),
                materialAssignment.feederId,
                twoDigitDecimalFormat.format(tray.firstComponentX),
                twoDigitDecimalFormat.format(tray.firstComponentY),
                twoDigitDecimalFormat.format(tray.lastComponentX),
                twoDigitDecimalFormat.format(tray.lastComponentY),
                tray.columns,
                tray.rows,
                tray.firstComponentIndex,
                tray.name,
                materialAssignment.component.description
            ]

            return trayRow
        }

        trayRows
    }


    Map<String, String> buildMaterial(Integer feederId, Feeder feeder, Component component) {

        //
        // Firmware < 2725B:
        //
        /*
        Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,SizeX,SizeY,HeightTake,DelayTake,nPullStripSpeed
        Station,0,29,4.17,0,12,??,3.75,0,6,0,0,0,0,0
         */
        //
        // SizeX/SizeY are INTEGER, machine accepts a range of 0.00 to 30.00 in the UI.
        // The when the BigDecimal (mm) values are stored in the DPV file they need to be multiplied by 100.  e.g. "0.01" -> "1" and "30.00" -> "3000"
        //
        // Firmware >= 2725B
        //
        /*
        Table,No.,ID,DeltX,DeltY,FeedRates,Note,Height,Speed,Status,nPixSizeX,nPixSizeY,HeightTake,DelayTake,nPullStripSpeed,nThreshold,nVisualRadio
        Station,0,4,-1.78,1.61,2,0402B103K500CT;Walsin Tech Corp;10nF 50V 0402 X7R 10%/CAP_0402,0.5,25,6,344,151,0,20,25,61,200
         */
        // nPixSizeX,nPixSizeY are INTEGER, UI defaults come from the SizeX/SizeY fields.
        // vision settings, when confirmed in UI, update nPixSizeX,nPixSizeY values.
        // The when the BigDecimal (mm) values are stored in the DPV file they need to be multiplied by 100.  e.g. "0.01" -> "1" and "30.00" -> "3000"
        //
        // All firmware:
        //
        // DelayTake is INTEGER, machine accepts a range of 0.00 to 3.00 in the UI.
        // The when the BigDecimal (second) values are stored in the DPV file they need to be multiplied by 100.  1 second = 100, 0.01 second = 1.

        PickSettings pickSettings = feeder.pickSettings

        int statusFlags = buildStatus(feeder.enabled, pickSettings)

        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")
        DecimalFormat zeroDigitDecimalFormat = new DecimalFormat("#0")

        Map<String, String> material = this.format.materialKeys.collectEntries { String key ->

            String value = null
            switch (key) {
                case 'ID':
                    value = feederId
                    break
                case 'DeltX':
                    value = twoDigitDecimalFormat.format(pickSettings.xOffset)
                    break
                case 'DeltY':
                    value = twoDigitDecimalFormat.format(pickSettings.yOffset)
                    break
                case 'FeedRates':
                    value = twoDigitDecimalFormat.format(pickSettings.tapeSpacing)
                    break
                case 'Note':
                    value = buildMaterialNote(component, feeder)
                    break
                case 'Height':
                    value = twoDigitDecimalFormat.format(component.height)
                    break
                case 'Speed':
                    value = buildPlaceSpeed(pickSettings.placeSpeedPercentage)
                    break
                case 'Status':
                    value = statusFlags & 0xFF
                    break
                case 'SizeX':
                case 'nPixSizeX':
                    BigDecimal width = 0.0
                    if (pickSettings.visionSize.isPresent()) {
                        width = pickSettings.visionSize.get().width
                    } else {
                        width = component.width
                    }
                    value = zeroDigitDecimalFormat.format(width * 100)
                    break
                case 'SizeY':
                case 'nPixSizeY':
                    BigDecimal length = 0.0
                    if (pickSettings.visionSize.isPresent()) {
                        length = pickSettings.visionSize.get().length
                    } else {
                        length = component.length
                    }
                    value = zeroDigitDecimalFormat.format(length * 100)
                    break
                case 'HeightTake':
                    value = zeroDigitDecimalFormat.format(pickSettings.takeHeight * 100)
                    break
                case 'DelayTake':
                    value = zeroDigitDecimalFormat.format(pickSettings.takeDelay * 100)
                    break
                case 'nPullStripSpeed':
                    value = pickSettings.pullSpeed
                    break
                case 'nThreshold':
                    if (pickSettings.visionSettings.isPresent()) {
                        value = pickSettings.visionSettings.get().visualThreshold
                    } else {
                        value = 0
                    }
                    break
                case 'nVisualRadio':
                    if (pickSettings.visionSettings.isPresent()) {
                        value = pickSettings.visionSettings.get().visualRadio
                    } else {
                        value = 0
                    }
                    break
            }
            [key, value]
        }

        // 'Table' and 'No.' are assigned later
        // Note: this method may generate a duplicate material, duplicates need to be removed before being written.
        assert material.size() == this.format.materialKeys.size()

        material
    }

    private String buildMaterialNote(Component component, Feeder feeder) {

        List<String> parts = []

        if (component.partCode && component.manufacturer) {
            parts << component.partCode
            parts << component.manufacturer
        }
        if (feeder.description) {
            parts << feeder.description
        }
        if (feeder.note) {
            parts << feeder.note
        }

        parts.join(";")
    }

    private int buildStatus(boolean enabled, PickSettings pickSettings) {
        int statusFlags = 0

        //76543210 bitmask
        //3: 1 = Separate Mount (don't use other heads)
        //3: 0 = Combine Mount (use other heads)
        //2: 1 = Use Vision
        //2: 0 = No Vision
        //1: 1 = Use Vacuum Detection
        //1: 0 = No Vacuum Detection
        //0: 1 = Skip placement
        //0: 0 = Place this component
        //Example: 3 (decimal) (0b00000011) = Skip placement, Use vacuum detection, No vision

        if (!enabled) {
            statusFlags |= (1 << 0)
        }

        if (pickSettings.checkVacuum) {
            statusFlags |= (1 << 1)
        }
        if (pickSettings.useVision) {
            statusFlags |= (1 << 2)
        }
        if (pickSettings.separateMount) {
            statusFlags |= (1 << 3)
        }
        statusFlags
    }

    void writeHeader(DPVHeader dpvHeader, Optional<Panel> optionalPanel) {

        Date now = new Date()
        String formattedDate = new SimpleDateFormat('yyyy/MM/dd').format(now)
        String formattedTime = new SimpleDateFormat('HH:mm:ss').format(now)

        String panelTypeValue = optionalPanel.present ? "1" : "0" // Type 0 = batch of PCBs. Type 1 = panel of PCBs.

        String content = "separated" + lineEnding +
            DPVFileHeaders.FILE + ",$dpvHeader.fileName" + lineEnding +
            DPVFileHeaders.PCBFILE + ",$dpvHeader.pcbFileName" + lineEnding +
            DPVFileHeaders.DATE + ",$formattedDate" + lineEnding +
            DPVFileHeaders.TIME + ",$formattedTime" + lineEnding +
            DPVFileHeaders.PANELTYPE + "," + panelTypeValue + lineEnding

        stream.print(content)
        stream.print(lineEnding)
    }

    String replaceCommasWithSemicolons(String value)
    {
        return value.replace(',', ';')
    }

    def writeMaterials(Map<Integer, Map<String, String>> materials) {

        String sectionHeader = this.format.materialKeys.join(',')
        stream.print(sectionHeader + tableLineEnding)

        materials.toSorted { a, b ->
            a.key <=> b.key
        }.each { Integer feederId, Map<String, String> material ->

            Map<String, String> managedColumns = [
                'Table': 'Station',
                'No.': materialNumberSequence.next().toString(),
            ]
            managedColumns.each {k, v ->
                material[k] = v
            }

            stream.print(material.values().collect(this.&replaceCommasWithSemicolons).join(",") + tableLineEnding)
        }
        stream.print(tableLineEnding)
    }


    void writePlacements(List<String[]> placements) {
        String sectionHeader =
            "Table,No.,ID,PHead,STNo.,DeltX,DeltY,Angle,Height,Skip,Speed,Explain,Note,Delay"

        stream.print(sectionHeader + tableLineEnding)

        placements.each { placement ->
            stream.print(placement.collect { it.replace(',', ';') }.join(",") + tableLineEnding)
        }
        stream.print(tableLineEnding)
    }

    void writeTrays(List<String[]> trays) {
        String sectionHeader = "Table,No.,ID,CenterX,CenterY,IntervalX,IntervalY,NumX,NumY,Start,Name,Component"

        stream.print(sectionHeader + tableLineEnding)

        trays.each { tray ->
            stream.print(tray.collect { it.replace(',', ';') }.join(",") + tableLineEnding)
        }

        stream.print(tableLineEnding)
    }

    void writePanel(Optional<Panel> optionalPanel) {
        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        if (optionalPanel.present) {
            Panel panel = optionalPanel.get()
            stream.print("Table,No.,ID,IntervalX,IntervalY,NumX,NumY" + tableLineEnding)
            stream.print("Panel_Array,0,1,${twoDigitDecimalFormat.format(panel.intervalX)},${twoDigitDecimalFormat.format(panel.intervalY)},${panel.numberX},${panel.numberY}" + tableLineEnding)
            stream.print(tableLineEnding)
        } else {
            stream.print("Table,No.,ID,DeltX,DeltY" + tableLineEnding)
            stream.print("Panel_Coord,0,1,0,0" + tableLineEnding)
            stream.print(tableLineEnding)
        }
    }

//    /**
//     * Note: this writes all the fiducial markers, regardless of how many there should be, checking for the correct
//     *       amount happens during argument processing, which may or may not be correct for the mode of the machine
//     *       i.e. need 2 for 2 point calibration, but the machine might be in 3 point calibration
//     *       nothing can be done about potential mismatches here.
//     * @param optionalFiducials
//     */
    void writeFiducials(Optional<List<Fiducial>> optionalFiducials) {
        DecimalFormat twoDigitDecimalFormat = new DecimalFormat("#0.##")

        NumberSequence fiducialNumberSequence = new NumberSequence(0)
        NumberSequence fiducialIDSequence = new NumberSequence(1)

        if (optionalFiducials.present) {

            //nType: 0 = use components, 1 = use marks
            //nFinished: ? 0 = calibration pending, 1 = calibration completed

            String calibationModeSectionHeader =
                "Table,No.,nType,nAlg,nFinished"

            stream.print(calibationModeSectionHeader + tableLineEnding)
            stream.print("PcbCalib,0,1,0,0" + lineEnding) // Note: NOT tableLineEnding
            stream.print(lineEnding) // Note: NOT tableLineEnding


            String calibationMarksSectionHeader =
                "Table,No.,ID,offsetX,offsetY,Note"

            stream.print(calibationMarksSectionHeader + tableLineEnding)

            List<Fiducial> fiducialList = optionalFiducials.get()

            fiducialList.each { fiducial ->
                String[] fiducialValues = [
                    "CalibPoint",
                    fiducialNumberSequence.next(),
                    fiducialIDSequence.next(),
                    twoDigitDecimalFormat.format(fiducial.coordinate.x),
                    twoDigitDecimalFormat.format(fiducial.coordinate.y),
                    fiducial.note
                ]
                stream.print(fiducialValues.collect { it.replace(',', ';') }.join(",") + lineEnding) // Note: NOT tableLineEnding
            }
            stream.print(lineEnding) // Note: NOT tableLineEnding
        }
    }

    void setAddPlacementsForFiducials(boolean addPlacementsForFiducialsEnabled) {
        this.addPlacementsForFiducialsEnabled = addPlacementsForFiducialsEnabled
    }
}