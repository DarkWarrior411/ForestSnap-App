# database.py
from sqlalchemy import create_engine, Column, Integer, Float, DateTime
from sqlalchemy.orm import sessionmaker, declarative_base
from datetime import datetime

# Creates a local SQLite file named 'forestsnap.db'
SQLALCHEMY_DATABASE_URL = "sqlite:///./forestsnap.db"

engine = create_engine(
    SQLALCHEMY_DATABASE_URL, connect_args={"check_same_thread": False}
)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)

Base = declarative_base()

# Define the Database Table
class AnalysisRecord(Base):
    __tablename__ = "analysis_records"

    id = Column(Integer, primary_key=True, index=True)
    timestamp = Column(DateTime, default=datetime.utcnow)
    latitude = Column(Float, index=True)
    longitude = Column(Float, index=True)
    fuel_load_score = Column(Float)
    dryness_risk_tier = Column(Integer)
    temperature_c = Column(Float)
    humidity_percent = Column(Integer)
    wind_speed_ms = Column(Float)
    final_fire_risk_percent = Column(Float)

# Create the tables in the database file
Base.metadata.create_all(bind=engine)
