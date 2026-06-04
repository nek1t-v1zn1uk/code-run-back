#!/bin/bash
set -e

echo "=========================================================="
echo "          CodeRun Full Installation Script"
echo "=========================================================="

# 1. Check for Frontend Repository
if [ ! -d "../code-run-web" ]; then
    echo "[1/4] Frontend repository not found at '../code-run-web'. Cloning..."
    git clone https://github.com/nek1t-v1zn1uk/code-run-web ../code-run-web
else
    echo "[1/4] Frontend repository found at '../code-run-web'."
fi

# 2. Setup Environment Variables
echo "[2/4] Setting up environment variables..."
if [ ! -f "deploy/.env" ]; then
    cp deploy/.env.example deploy/.env
    # Generate random secure passwords for the .env
    DB_PASS=$(openssl rand -hex 16)
    RABBIT_PASS=$(openssl rand -hex 16)
    JWT=$(openssl rand -base64 32 | tr -dc 'a-zA-Z0-9' | head -c 48)
    ADMIN_PASS=$(openssl rand -base64 12)
    
    sed -i "s/change_me_in_production/$DB_PASS/1" deploy/.env
    sed -i "s/change_me_in_production/$RABBIT_PASS/1" deploy/.env
    sed -i "s/change_me_in_production/$ADMIN_PASS/1" deploy/.env
    sed -i "s/change_me_to_random_base64_string/$JWT/" deploy/.env
    echo "Generated secure deploy/.env file."
    echo "=========================================================="
    echo "IMPORTANT: Admin Account Credentials"
    echo "Email: admin@coderun.local"
    echo "Password: $ADMIN_PASS"
    echo "Please save this password securely!"
    echo "=========================================================="
else
    echo "deploy/.env already exists."
fi

# 3. Setup Isolate Daemon
echo "[3/4] Building and setting up CodeRun Isolate Daemon..."
cd code-run-isolate-daemon
make
sudo mkdir -p /opt/code-run-isolate-daemon
sudo cp code-run-isolate-daemon /opt/code-run-isolate-daemon/
sudo cp code-run-isolate-daemon.service /etc/systemd/system/
sudo systemctl daemon-reload
sudo systemctl enable --now code-run-isolate-daemon.service
cd ..

# 4. Bring up Docker Containers
echo "[4/4] Starting Docker infrastructure..."
cd deploy
docker compose up -d --build
cd ..

echo "=========================================================="
echo "Installation complete!"
echo "Your production cluster is now spinning up."
echo "You can check the logs using: cd deploy && docker compose logs -f"
echo "=========================================================="
