package cn.zdjc.wms.project.control.websocket;

import cn.zdjc.wms.project.common.constant.ProjectConstant;
import cn.zdjc.wms.project.common.socket.TerminalWebSocketEndpoint;
import cn.zdjc.wms.project.erp.dto.ResultWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ProjectConstant.PREFIX_CONTEXT_PATH + "/websocket")
@Slf4j
public class WebSocketController {

    @GetMapping("/online-count")
    public ResultWrapper<Integer> getOnlineCount() {
        return ResultWrapper.buildSuccess(TerminalWebSocketEndpoint.getOnlineCount());
    }

    @GetMapping("/terminals")
    public ResultWrapper<List<TerminalWebSocketEndpoint.TerminalInfo>> getTerminals() {
        return ResultWrapper.buildSuccess(TerminalWebSocketEndpoint.getAllTerminalInfo());
    }

    @PostMapping("/send/{terminalId}")
    public ResultWrapper<Boolean> sendToTerminal(@PathVariable String terminalId,
                                          @RequestBody Object message) {
        boolean success = TerminalWebSocketEndpoint.sendToTerminal(terminalId, message);
        return ResultWrapper.buildSuccess(success);
    }

    @PostMapping("/broadcast")
    public ResultWrapper<Boolean> broadcast(@RequestBody Object message) {
        TerminalWebSocketEndpoint.broadcast(message);
        return ResultWrapper.buildSuccess(true);
    }
}