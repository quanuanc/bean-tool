use std::fs;
use std::path::Path;
use clap::Parser;

#[derive(Parser, Debug)]
#[command(author, version, about, long_about = None)]
struct Args {
    #[arg(help = "The command, only 'remove-meta' support currently.")]
    command: String,

    #[arg(long, help = "Specify the txn path, will process all .beancount files recursively.")]
    txn_path: String,
}

fn main() {
    let args = Args::parse();

    if args.command != "remove-meta" {
        println!("Command '{}' not supported", args.command);
        return;
    }

    if !Path::new(&args.txn_path).exists() {
        println!("txnPath does not exist!");
        return;
    }

    process_beancount_files(&args.txn_path);
}

fn process_beancount_files(directory: &str) {
    let paths = fs::read_dir(directory).unwrap();

    for entry in paths {
        let entry = entry.unwrap();
        let path = entry.path();

        if path.is_dir() {
            // Recursively process subdirectories
            process_beancount_files(path.to_str().unwrap());
        } else if path.is_file() && path.extension().unwrap_or_default() == "beancount" {
            process_file(path.to_str().unwrap());
        }
    }
}

fn process_file(path: &str) {
    let metas = vec![
        "category: ", "method: ", "orderId: ", "payTime: ", "source: ",
        "status: ", "type: ", "alipay_trade_no: ", "wechat_trade_no: ",
        "  note: ", "shop_trade_no: ", "timestamp: ", "trade_time: ",
        "balances: ", "currency: ", "peerAccount: ", "txType: ", "type: ",
        "trade_time: ", "merchantId: ", "peerAccountNum: ",
    ];

    let content = fs::read_to_string(path).unwrap();
    let lines: Vec<&str> = content.lines().collect();
    let mut file_modified = false;

    let filtered_lines: Vec<&str> = lines
        .into_iter()
        .filter(|line| {
            for meta in &metas {
                if line.contains(meta) {
                    file_modified = true;
                    return false;
                }
            }
            true
        })
        .collect();

    if file_modified {
        let new_content = filtered_lines.join("\n");
        fs::write(path, new_content).unwrap();
    }
}
