

import Viewer from 'bpmn-js/lib/Viewer';

window.createViewer = function(xml) {

    var exampleXML = xml || "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" xmlns:dc=\"http://www.omg.org/spec/DD/20100524/DC\" id=\"Definitions_1dj613m\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"3.0.0-dev\">\n" +
        "  <bpmn:process id=\"Process_1fuv6j1\" isExecutable=\"true\">\n" +
        "    <bpmn:startEvent id=\"StartEvent_1\" />\n" +
        "  </bpmn:process>\n" +
        "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
        "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_1fuv6j1\">\n" +
        "      <bpmndi:BPMNShape id=\"_BPMNShape_StartEvent_2\" bpmnElement=\"StartEvent_1\">\n" +
        "        <dc:Bounds x=\"179\" y=\"159\" width=\"36\" height=\"36\" />\n" +
        "      </bpmndi:BPMNShape>\n" +
        "    </bpmndi:BPMNPlane>\n" +
        "  </bpmndi:BPMNDiagram>\n" +
        "</bpmn:definitions>\n";




    var viewer = new Viewer({
        container: '#canvas'
    });

    var elementRegistry = viewer.get('elementRegistry');

    viewer.importXML(exampleXML, function (err) {
        if (!err) {
            viewer.get('canvas').zoom('fit-viewport');
        } else {
            sessionStorage.clear()
            console.log('something went wrong:', err);
        }
    });




    window.bpmnjs = viewer;
    window.bpmnjs_elReg = elementRegistry;
}