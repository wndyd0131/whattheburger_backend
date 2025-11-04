docker build -t whattheburger_backend .
docker tag whattheburger_backend wndyd0131/whattheburger_backend:latest
docker push wndyd0131/whattheburger_backend:latest
#docker compose down -v
#docker compose up --build