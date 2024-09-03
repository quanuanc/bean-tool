import ArgumentParser
import Foundation

@main
struct BeanTool: ParsableCommand {
    @Argument(help: "The command, only 'remove-meta' support currently.")
    var command: String

    @Option(help: "Specify the txn path, only support absolute path.")
    var txnPath: String

    public func run() throws {
        guard command == "remove-meta" else {
            print("Command '\(command)' not support")
            return
        }

        var isDir: ObjCBool = false
        let fileManager = FileManager.default
        let fileExists = fileManager.fileExists(atPath: txnPath, isDirectory: &isDir)

        guard fileExists else {
            print("txnPath does not exists!")
            return
        }
        processBeancountFiles(in: txnPath)
    }

    func processBeancountFiles(in directory: String) {
        let fileManager = FileManager.default
        guard let enumerator = fileManager.enumerator(atPath: directory) else {
            print("Can not enumerate directory!")
            return
        }

        for case let filePath as String in enumerator {
            if filePath.hasSuffix(".beancount") {
                let fullPath = (directory as NSString).appendingPathComponent(filePath)
                processFile(atPath: fullPath)
            }
        }
    }

    func processFile(atPath path: String) {
        let metas = [
            "category: ",
            "method: ",
            "orderId: ",
            "payTime: ",
            "source: ",
            "status: ",
            "type: ",
            "alipay_trade_no: ",
            "wechat_trade_no: ",
            "  note: ",
            "shop_trade_no: ",
            "timestamp: ",
            "trade_time: ",
            "balances: ",
            "currency: ",
            "peerAccount: ",
            "txType: ",
            "type: ",
            "trade_time: ",
            "merchantId: ",
        ]
        var fileModified = false
        do {
            let content = try String(contentsOfFile: path, encoding: .utf8)
            let lines = content.split(separator: "\n")
            let filteredLines = lines.filter {
                for meta in metas {
                    if $0.contains(meta) {
                        fileModified = true
                        return false
                    }
                }
                return true
            }
            if fileModified {
                let newContent = filteredLines.joined(separator: "\n")
                try newContent.write(toFile: path, atomically: true, encoding: .utf8)
            }
        } catch {
            print("Error in process file: \(path)")
        }
    }
}
