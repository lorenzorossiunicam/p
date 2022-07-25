

import Modeler from 'bpmn-js/lib/Modeler';
import Viewer from "bpmn-js/lib/Viewer";

window.createModeler = function() {

// create a modeler
    var exampleXML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
        "<bpmn:definitions xmlns:bpmn=\"http://www.omg.org/spec/BPMN/20100524/MODEL\" xmlns:bpmndi=\"http://www.omg.org/spec/BPMN/20100524/DI\" id=\"Definitions_0j5119v\" targetNamespace=\"http://bpmn.io/schema/bpmn\" exporter=\"Camunda Modeler\" exporterVersion=\"3.1.0\">\n" +
        "  <bpmn:process id=\"Process_0jpbiuf\" isExecutable=\"true\" />\n" +
        "  <bpmndi:BPMNDiagram id=\"BPMNDiagram_1\">\n" +
        "    <bpmndi:BPMNPlane id=\"BPMNPlane_1\" bpmnElement=\"Process_0jpbiuf\" />\n" +
        "  </bpmndi:BPMNDiagram>\n" +
        "</bpmn:definitions>\n";

    var propertiesPanelModule = require('bpmn-js-properties-panel'),
        propertiesProviderModule = require('bpmn-js-properties-panel');



    var modeler = new Modeler({
        container: '#canvas',
        additionalModules: [
            propertiesProviderModule,
            propertiesPanelModule
        ],
        propertiesPanel: {
            parent: '#js-properties-panel'
        }
    });


    var elementRegistry = modeler.get('elementRegistry');

    modeler.importXML(exampleXML, function (err) {
        if (!err) {
            modeler.get('canvas').zoom('fit-viewport');
        } else {
            sessionStorage.clear()
            console.log('something went wrong:', err);
        }
    });

    window.bpmnjs = modeler;
    window.bpmnjs_elReg = elementRegistry;

}
